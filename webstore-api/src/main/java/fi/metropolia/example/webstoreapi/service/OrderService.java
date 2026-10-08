package fi.metropolia.example.webstoreapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.CustomerAddressDto;
import fi.metropolia.example.webstoreapi.dto.OrderAggregate;
import fi.metropolia.example.webstoreapi.dto.OrderCreateRequest;
import fi.metropolia.example.webstoreapi.dto.OrderDetailDto;
import fi.metropolia.example.webstoreapi.dto.OrderItemRequest;
import fi.metropolia.example.webstoreapi.dto.OrderItemViewDto;
import fi.metropolia.example.webstoreapi.dto.OrderStatusUpdateRequest;
import fi.metropolia.example.webstoreapi.dto.OrderSummaryDto;
import fi.metropolia.example.webstoreapi.dto.CartEditRequest;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.entity.Customer;
import fi.metropolia.example.webstoreapi.entity.CustomerAddress;
import fi.metropolia.example.webstoreapi.entity.Order;
import fi.metropolia.example.webstoreapi.entity.OrderItem;
import fi.metropolia.example.webstoreapi.entity.OrderStatus;
import fi.metropolia.example.webstoreapi.entity.Product;
import fi.metropolia.example.webstoreapi.exception.ConflictException;
import fi.metropolia.example.webstoreapi.exception.InsufficientStockException;
import fi.metropolia.example.webstoreapi.exception.InvalidOrderStateException;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.CustomerAddressRepository;
import fi.metropolia.example.webstoreapi.repository.CustomerRepository;
import fi.metropolia.example.webstoreapi.repository.OrderItemRepository;
import fi.metropolia.example.webstoreapi.repository.OrderRepository;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

/**
 * Order aggregate business logic (plan §2–§5): listing/searching orders,
 * checkout with pessimistic stock locking, transactional cart editing and the
 * order status lifecycle.
 */
@Service
public class OrderService {

	/** Default delivery promise added at checkout. */
	private static final int DEFAULT_DELIVERY_DAYS = 3;

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CustomerRepository customerRepository;
	private final CustomerAddressRepository customerAddressRepository;
	private final ProductRepository productRepository;

	public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			CustomerRepository customerRepository, CustomerAddressRepository customerAddressRepository,
			ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.customerRepository = customerRepository;
		this.customerAddressRepository = customerAddressRepository;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public OrderDetailDto getOrder(Integer id) {
		return toDetail(findOrder(id));
	}

	/**
	 * Order list / history / dynamic search (plan §4 #9, #11, #12). All filter
	 * parameters are optional; the customer is JOIN FETCHed for the name and
	 * the per-order aggregates are fetched in one query for the whole page.
	 */
	@Transactional(readOnly = true)
	public PageResponseDto<OrderSummaryDto> searchOrders(OrderStatus status, Integer customerId,
			LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
		if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
			throw new IllegalArgumentException("dateFrom must not be after dateTo");
		}
		Page<Order> orders = orderRepository.findAll(orderSpecification(status, customerId, dateFrom, dateTo), pageable);
		Map<Integer, OrderAggregate> aggregates = aggregateOrders(orders.getContent());
		return PageResponseDto.of(orders.map(order -> toSummary(order, aggregates.get(order.getId()))));
	}

	/**
	 * Checkout (plan §4 #13): inserts the order + items and decrements stock in
	 * one transaction. The involved product rows are locked with
	 * {@code SELECT ... FOR UPDATE} first, so concurrent checkouts cannot
	 * oversell; insufficient stock aborts the whole transaction with 409.
	 */
	@Transactional
	public OrderDetailDto checkout(OrderCreateRequest request) {
		if (request == null || request.customerId() == null) {
			throw new IllegalArgumentException("customerId is required");
		}
		List<OrderItemRequest> lines = validateOrderLines(request.items());
		Customer customer = customerRepository.findById(request.customerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + request.customerId()));
		CustomerAddress shippingAddress = resolveShippingAddress(request.shippingAddressId(), customer);
		Map<Integer, Product> products = lockProducts(lines);

		for (OrderItemRequest line : lines) {
			reserveStock(products.get(line.productId()), line.quantity());
		}

		Order order = new Order();
		order.setCustomer(customer);
		order.setShippingAddress(shippingAddress);
		order.setOrderDate(LocalDateTime.now());
		order.setDeliveryDate(order.getOrderDate().plusDays(DEFAULT_DELIVERY_DAYS));
		order.setStatus(OrderStatus.NEW);
		order = orderRepository.save(order);

		for (OrderItemRequest line : lines) {
			Product product = products.get(line.productId());
			OrderItem item = newItem(order, product, line.quantity());
			orderItemRepository.save(item);
			order.getItems().add(item);
		}
		return toDetail(order);
	}

	/**
	 * Cart editing while the order is still NEW (plan §4 #15): add lines,
	 * change quantities, remove lines; stock is decremented/returned in the
	 * same transaction, again under pessimistic product locks.
	 */
	@Transactional
	public OrderDetailDto editCart(Integer orderId, CartEditRequest request) {
		Order order = findOrder(orderId);
		if (order.getStatus() != OrderStatus.NEW) {
			throw new InvalidOrderStateException(
					"Order " + orderId + " is " + order.getStatus() + "; only NEW orders can be edited");
		}
		List<OrderItemRequest> addLines = request == null || request.add() == null ? List.of() : request.add();
		List<OrderItemRequest> updateLines = request == null || request.update() == null ? List.of() : request.update();
		List<Integer> removeIds = request == null || request.remove() == null ? List.of() : request.remove();

		Set<Integer> touched = new TreeSet<>();
		validateEditLines(addLines, touched);
		validateEditLines(updateLines, touched);
		for (Integer productId : removeIds) {
			if (productId == null) {
				throw new IllegalArgumentException("remove entries must be product ids");
			}
			if (!touched.add(productId)) {
				throw new IllegalArgumentException("product appears in more than one operation: id=" + productId);
			}
		}
		if (touched.isEmpty()) {
			throw new IllegalArgumentException("no cart changes requested");
		}

		Map<Integer, Product> products = lockProductsByIds(touched);
		Map<Integer, OrderItem> itemsByProduct = order.getItems().stream()
				.collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

		for (OrderItemRequest line : addLines) {
			if (itemsByProduct.containsKey(line.productId())) {
				throw new ConflictException("Product " + line.productId()
						+ " is already in order " + orderId + "; use update to change its quantity");
			}
			Product product = products.get(line.productId());
			reserveStock(product, line.quantity());
			OrderItem item = newItem(order, product, line.quantity());
			orderItemRepository.save(item);
			order.getItems().add(item);
			itemsByProduct.put(product.getId(), item);
		}

		for (OrderItemRequest line : updateLines) {
			OrderItem item = itemsByProduct.get(line.productId());
			if (item == null) {
				throw new ResourceNotFoundException(
						"Product " + line.productId() + " is not in order " + orderId);
			}
			Product product = products.get(line.productId());
			int delta = line.quantity() - item.getQuantity();
			if (delta > 0) {
				reserveStock(product, delta);
			}
			else if (delta < 0) {
				product.setStockQuantity(product.getStockQuantity() - delta);
			}
			item.setQuantity(line.quantity());
		}

		for (Integer productId : removeIds) {
			OrderItem item = itemsByProduct.get(productId);
			if (item == null) {
				throw new ResourceNotFoundException("Product " + productId + " is not in order " + orderId);
			}
			Product product = products.get(productId);
			product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
			order.getItems().remove(item);
			orderItemRepository.delete(item);
		}
		return toDetail(order);
	}

	/**
	 * Order status transition (plan §4 #14). NEW → SHIPPED | CANCELLED,
	 * SHIPPED → DELIVERED; cancelling returns the reserved stock.
	 */
	@Transactional
	public OrderDetailDto changeStatus(Integer orderId, OrderStatusUpdateRequest request) {
		Order order = findOrder(orderId);
		OrderStatus current = order.getStatus();
		OrderStatus target = parseStatus(request == null ? null : request.status());
		validateTransition(current, target);
		if (target == OrderStatus.CANCELLED) {
			restoreStock(new ArrayList<>(order.getItems()));
		}
		order.setStatus(target);
		return toDetail(orderRepository.save(order));
	}

	/**
	 * Admin delete of a cancelled order (plan §4 #21). The provided schema's FK
	 * has no ON DELETE CASCADE, so the lines are deleted explicitly in the
	 * same transaction.
	 */
	@Transactional
	public void deleteOrder(Integer orderId) {
		Order order = findOrder(orderId);
		if (order.getStatus() != OrderStatus.CANCELLED) {
			throw new InvalidOrderStateException(
					"Only CANCELLED orders can be deleted; order " + orderId + " is " + order.getStatus());
		}
		orderItemRepository.deleteAll(order.getItems());
		order.getItems().clear();
		orderRepository.delete(order);
	}

	private Order findOrder(Integer id) {
		return orderRepository.findWithItemsById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));
	}

	private CustomerAddress resolveShippingAddress(Integer addressId, Customer customer) {
		if (addressId == null) {
			return null;
		}
		CustomerAddress address = customerAddressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found: id=" + addressId));
		if (!address.getCustomer().getId().equals(customer.getId())) {
			throw new ConflictException(
					"Address " + addressId + " does not belong to customer " + customer.getId());
		}
		return address;
	}

	private List<OrderItemRequest> validateOrderLines(List<OrderItemRequest> lines) {
		if (lines == null || lines.isEmpty()) {
			throw new IllegalArgumentException("at least one order item is required");
		}
		Set<Integer> seen = new LinkedHashSet<>();
		for (OrderItemRequest line : lines) {
			if (line == null || line.productId() == null) {
				throw new IllegalArgumentException("each order item needs a productId");
			}
			if (line.quantity() == null || line.quantity() <= 0) {
				throw new IllegalArgumentException("quantity must be > 0 for product " + line.productId());
			}
			if (!seen.add(line.productId())) {
				throw new IllegalArgumentException("duplicate product in order: id=" + line.productId());
			}
		}
		return lines;
	}

	private void validateEditLines(List<OrderItemRequest> lines, Set<Integer> touched) {
		for (OrderItemRequest line : lines) {
			if (line == null || line.productId() == null) {
				throw new IllegalArgumentException("each cart change needs a productId");
			}
			if (line.quantity() == null || line.quantity() <= 0) {
				throw new IllegalArgumentException("quantity must be > 0 for product " + line.productId());
			}
			if (!touched.add(line.productId())) {
				throw new IllegalArgumentException("product appears in more than one operation: id=" + line.productId());
			}
		}
	}

	private Map<Integer, Product> lockProducts(List<OrderItemRequest> lines) {
		Set<Integer> ids = lines.stream().map(OrderItemRequest::productId)
				.collect(Collectors.toCollection(TreeSet::new));
		return lockProductsByIds(ids);
	}

	/** Locks product rows in id order and fails with 404 on unknown ids. */
	private Map<Integer, Product> lockProductsByIds(Collection<Integer> ids) {
		Set<Integer> sortedIds = new TreeSet<>(ids);
		if (sortedIds.isEmpty()) {
			return Map.of();
		}
		Map<Integer, Product> byId = productRepository.findAllByIdForUpdate(sortedIds).stream()
				.collect(Collectors.toMap(Product::getId, Function.identity()));
		if (byId.size() != sortedIds.size()) {
			Set<Integer> missing = new TreeSet<>(sortedIds);
			missing.removeAll(byId.keySet());
			throw new ResourceNotFoundException("Product not found: id=" + missing);
		}
		return byId;
	}

	private void reserveStock(Product product, int quantity) {
		if (product.getStockQuantity() < quantity) {
			throw new InsufficientStockException(product.getId(), product.getStockQuantity(), quantity);
		}
		product.setStockQuantity(product.getStockQuantity() - quantity);
	}

	private void restoreStock(List<OrderItem> items) {
		if (items.isEmpty()) {
			return;
		}
		Map<Integer, Product> products = lockProductsByIds(items.stream().map(OrderItem::getProductId).toList());
		for (OrderItem item : items) {
			Product product = products.get(item.getProductId());
			product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
		}
	}

	private OrderItem newItem(Order order, Product product, int quantity) {
		OrderItem item = new OrderItem();
		item.setOrderId(order.getId());
		item.setProductId(product.getId());
		item.setOrder(order);
		item.setProduct(product);
		item.setQuantity(quantity);
		item.setUnitPrice(product.getPrice());
		return item;
	}

	private OrderStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("status is required");
		}
		try {
			return OrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException(
					"Unknown order status '" + value + "'; expected NEW, SHIPPED, DELIVERED or CANCELLED");
		}
	}

	private void validateTransition(OrderStatus from, OrderStatus to) {
		boolean allowed = from != null && switch (from) {
			case NEW -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
			case SHIPPED -> to == OrderStatus.DELIVERED;
			default -> false;
		};
		if (!allowed) {
			throw new InvalidOrderStateException("Cannot change order status from " + from + " to " + to);
		}
	}

	private Specification<Order> orderSpecification(OrderStatus status, Integer customerId, LocalDateTime dateFrom,
			LocalDateTime dateTo) {
		return (root, query, cb) -> {
			if (query.getResultType() != Long.class && query.getResultType() != long.class) {
				root.fetch("customer", JoinType.LEFT);
			}
			List<Predicate> predicates = new ArrayList<>();
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (customerId != null) {
				predicates.add(cb.equal(root.get("customer").get("id"), customerId));
			}
			if (dateFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom));
			}
			if (dateTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), dateTo));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private Map<Integer, OrderAggregate> aggregateOrders(List<Order> orders) {
		List<Integer> ids = orders.stream().map(Order::getId).toList();
		if (ids.isEmpty()) {
			return Map.of();
		}
		return orderItemRepository.aggregateForOrders(ids).stream()
				.collect(Collectors.toMap(OrderAggregate::getOrderId, Function.identity()));
	}

	private OrderSummaryDto toSummary(Order order, OrderAggregate aggregate) {
		Customer customer = order.getCustomer();
		long itemCount = aggregate != null && aggregate.getItemCount() != null ? aggregate.getItemCount() : 0L;
		BigDecimal totalAmount = aggregate != null && aggregate.getTotalAmount() != null
				? aggregate.getTotalAmount() : BigDecimal.ZERO;
		return new OrderSummaryDto(order.getId(), order.getOrderDate(),
				order.getStatus() != null ? order.getStatus().name() : null, customer.getId(),
				customer.getFirstName() + " " + customer.getLastName(), itemCount, totalAmount);
	}

	private OrderDetailDto toDetail(Order order) {
		List<OrderItemViewDto> items = order.getItems().stream()
				.map(item -> new OrderItemViewDto(item.getProductId(), item.getProduct().getName(),
						item.getQuantity(), item.getUnitPrice(),
						item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
				.toList();
		BigDecimal totalAmount = items.stream().map(OrderItemViewDto::subtotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		CustomerAddress address = order.getShippingAddress();
		CustomerAddressDto addressDto = address == null ? null
				: new CustomerAddressDto(address.getId(), address.getCustomer().getId(), address.getStreetAddress(),
						address.getPostalCode(), address.getCity(), address.getCountry());
		Customer customer = order.getCustomer();
		return new OrderDetailDto(order.getId(), customer.getId(),
				customer.getFirstName() + " " + customer.getLastName(), order.getOrderDate(), order.getDeliveryDate(),
				order.getStatus() != null ? order.getStatus().name() : null, addressDto, items, totalAmount);
	}

}

package fi.metropolia.example.webstoreapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.ContactDto;
import fi.metropolia.example.webstoreapi.dto.CustomerAddressDto;
import fi.metropolia.example.webstoreapi.dto.CustomerDetailDto;
import fi.metropolia.example.webstoreapi.dto.CustomerDto;
import fi.metropolia.example.webstoreapi.dto.CustomerOrderStats;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.TopSpenderDto;
import fi.metropolia.example.webstoreapi.entity.Customer;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.ContactRepository;
import fi.metropolia.example.webstoreapi.repository.CustomerAddressRepository;
import fi.metropolia.example.webstoreapi.repository.CustomerRepository;
import fi.metropolia.example.webstoreapi.repository.OrderRepository;

/**
 * Customer read side of the webshop view (plan §2: customers are not editable
 * through the shop API) plus the cross-table customer queries (plan §4 #6–#8).
 */
@Service
public class CustomerService {

	private final CustomerRepository customerRepository;
	private final CustomerAddressRepository customerAddressRepository;
	private final ContactRepository contactRepository;
	private final OrderRepository orderRepository;

	public CustomerService(CustomerRepository customerRepository, CustomerAddressRepository customerAddressRepository,
			ContactRepository contactRepository, OrderRepository orderRepository) {
		this.customerRepository = customerRepository;
		this.customerAddressRepository = customerAddressRepository;
		this.contactRepository = contactRepository;
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public PageResponseDto<CustomerDto> listCustomers(String search, Pageable pageable) {
		return PageResponseDto.of(customerRepository.findAll(searchSpecification(search), pageable).map(this::toDto));
	}

	/**
	 * Customer detail combining {@code customers} + {@code contacts} (matched
	 * by email) + {@code customeraddresses} + order-history aggregates from
	 * {@code orders}/{@code orderitems} (plan §4 #6).
	 */
	@Transactional(readOnly = true)
	public CustomerDetailDto getCustomer(Integer id) {
		Customer customer = findCustomer(id);
		List<CustomerAddressDto> addresses = customerAddressRepository.findByCustomerId(id).stream()
				.map(address -> new CustomerAddressDto(address.getId(), address.getCustomer().getId(),
						address.getStreetAddress(), address.getPostalCode(), address.getCity(), address.getCountry()))
				.toList();
		ContactDto contact = contactRepository.findFirstByEmailIgnoreCaseOrderByIdAsc(customer.getEmail())
				.map(c -> new ContactDto(c.getId(), c.getEmail(), c.getReference()))
				.orElse(null);
		CustomerOrderStats stats = orderRepository.findCustomerOrderStats(id);
		long orderCount = stats != null && stats.getOrderCount() != null ? stats.getOrderCount() : 0L;
		BigDecimal totalSpent = stats != null && stats.getTotalSpent() != null ? stats.getTotalSpent()
				: BigDecimal.ZERO;
		LocalDateTime lastOrderDate = stats != null ? stats.getLastOrderDate() : null;
		return new CustomerDetailDto(customer.getId(), customer.getFirstName(), customer.getLastName(),
				customer.getEmail(), customer.getPhone(), contact, addresses, orderCount, totalSpent, lastOrderDate);
	}

	/** LEFT JOIN/NOT EXISTS subquery: customers with no orders (plan §4 #7). */
	@Transactional(readOnly = true)
	public PageResponseDto<CustomerDto> customersWithoutOrders(Pageable pageable) {
		return PageResponseDto.of(customerRepository.findCustomersWithoutOrders(pageable).map(this::toDto));
	}

	/** Ranked from the {@code customer_summary} view (ORDER BY total_spent, plan §4 #8). */
	@Transactional(readOnly = true)
	public List<TopSpenderDto> topSpenders(int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("limit must be > 0");
		}
		return orderRepository.findTopSpenders(limit).stream()
				.map(row -> new TopSpenderDto(row.getCustomerId(), row.getCustomerName(), row.getEmail(),
						row.getOrderCount(), row.getTotalSpent()))
				.toList();
	}

	public Customer findCustomer(Integer id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
	}

	private Specification<Customer> searchSpecification(String search) {
		return (root, query, cb) -> {
			if (search == null || search.isBlank()) {
				return cb.conjunction();
			}
			String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
			return cb.or(cb.like(cb.lower(root.get("firstName")), pattern),
					cb.like(cb.lower(root.get("lastName")), pattern),
					cb.like(cb.lower(root.get("email")), pattern));
		};
	}

	private CustomerDto toDto(Customer customer) {
		return new CustomerDto(customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getEmail(),
				customer.getPhone());
	}

}

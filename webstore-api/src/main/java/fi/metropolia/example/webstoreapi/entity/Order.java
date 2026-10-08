package fi.metropolia.example.webstoreapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Order (order header), mapped to {@code orders}.
 *
 * <p>Association notes:
 * <ul>
 * <li><b>1:M customer → orders:</b> this entity holds FK {@code customer_id},
 * the owning side. Customer is LAZY — order overviews don't need the whole
 * customer record; customer data is joined explicitly where it is
 * displayed.</li>
 * <li><b>1:1 Order ↔ CustomerAddress:</b> FK {@code shipping_address_id}
 * (navigable only from this side; the DB does not enforce uniqueness, several
 * orders of the same customer can reuse the same shipping address).</li>
 * <li><b>N:M Orders ↔ Products:</b> through {@link OrderItem} ({@code orderitems}
 * is the join table, composite PK (order_id, product_id)).</li>
 * <li><b>status:</b> {@link OrderStatus}, persisted via
 * {@code OrderStatusConverter} (autoApply).</li>
 * </ul></p>
 */
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Column(name = "order_date", nullable = false)
	private LocalDateTime orderDate;

	@Nullable
	@Column(name = "delivery_date")
	private LocalDateTime deliveryDate;

	@Nullable
	@OneToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "shipping_address_id")
	private CustomerAddress shippingAddress;

	@Nullable
	@Column(name = "status", length = 50)
	private OrderStatus status;

	@OneToMany(mappedBy = "order")
	private List<OrderItem> items = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public LocalDateTime getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(LocalDateTime deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public CustomerAddress getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(CustomerAddress shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + (id != null ? "id=" + id + ", " : "") + "status=" + status + ")";
	}

}

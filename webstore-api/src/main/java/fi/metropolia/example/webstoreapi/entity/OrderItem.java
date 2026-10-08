package fi.metropolia.example.webstoreapi.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Order item (line of an order), mapped to {@code orderitems}.
 *
 * <p>This table is the <b>join table</b> of the N:M Orders ↔ Products
 * association — it carries association attributes (quantity, unit_price) in
 * addition to the composite PK {@code (order_id, product_id)}, which is why it
 * is modelled as an entity with {@link IdClass} {@link OrderItemId} rather
 * than a hidden {@code @ManyToMany} join table (attributes in a join table
 * always require an explicit entity).
 *
 * <p>basic {@code orderId}/{@code productId} id fields are the actual FK
 * columns; the {@link ManyToOne} navigations are mapped
 * {@code insertable=false, updatable=false} onto the same columns so both the
 * JPA relationship and the ids can be set (create flows work either way).
 * Both navigations are LAZY — the order details endpoint JOIN FETCHes what it
 * shows.</p>
 */
@Entity
@Table(name = "orderitems")
@IdClass(OrderItemId.class)
public class OrderItem {

	@Id
	@Column(name = "order_id", nullable = false, updatable = false)
	private Integer orderId;

	@Id
	@Column(name = "product_id", nullable = false, updatable = false)
	private Integer productId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, insertable = false, updatable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
	private Product product;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(orderId=" + orderId + ", productId=" + productId + ", quantity="
				+ quantity + ", unitPrice=" + unitPrice + ")";
	}

}

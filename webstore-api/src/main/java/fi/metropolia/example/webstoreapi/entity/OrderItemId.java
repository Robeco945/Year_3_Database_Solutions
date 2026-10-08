package fi.metropolia.example.webstoreapi.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite id for {@link OrderItem}: {@code orderitems} is the link table of
 * the N:M Orders ↔ Products association, PK (order_id, product_id).
 */
public class OrderItemId implements Serializable {

	private Integer orderId;

	private Integer productId;

	public OrderItemId() {
	}

	public OrderItemId(Integer orderId, Integer productId) {
		this.orderId = orderId;
		this.productId = productId;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public Integer getProductId() {
		return productId;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof OrderItemId other)) {
			return false;
		}
		return Objects.equals(orderId, other.orderId) && Objects.equals(productId, other.productId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(orderId, productId);
	}

}

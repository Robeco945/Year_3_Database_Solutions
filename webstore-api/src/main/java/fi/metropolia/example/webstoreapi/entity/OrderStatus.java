package fi.metropolia.example.webstoreapi.entity;

/**
 * Order lifecycle status, stored in {@code orders.status} (varchar, values
 * observed in the data: NEW, SHIPPED, CANCELLED; DELIVERED reserved for the
 * final transition SHIPPED → DELIVERED, plan §2).
 *
 * <p>Persisted via {@code OrderStatusConverter} (AttributeConverter), not as an
 * ordinal.</p>
 */
public enum OrderStatus {

	/** Order placed, items may still be edited (cart phase). */
	NEW,

	/** Order shipped; items locked, awaiting delivery. */
	SHIPPED,

	/** Order delivered to the customer (final state). */
	DELIVERED,

	/** Order cancelled; reserved stock is returned (plan §5). */
	CANCELLED

}

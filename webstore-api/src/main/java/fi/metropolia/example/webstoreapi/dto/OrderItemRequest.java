package fi.metropolia.example.webstoreapi.dto;

/**
 * One product line of a checkout request (plan §4 #13): product id + quantity.
 * The unit price is always taken from the product row inside the transaction,
 * never from the client.
 */
public record OrderItemRequest(Integer productId, Integer quantity) {
}

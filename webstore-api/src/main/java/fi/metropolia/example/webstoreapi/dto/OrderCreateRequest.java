package fi.metropolia.example.webstoreapi.dto;

import java.util.List;

/**
 * Checkout request body (plan §4 #13): customer, optional shipping address and
 * the ordered lines. Creates the order and its items and decrements stock in
 * one transaction.
 */
public record OrderCreateRequest(Integer customerId, Integer shippingAddressId, List<OrderItemRequest> items) {
}

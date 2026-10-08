package fi.metropolia.example.webstoreapi.dto;

import java.util.List;

/**
 * Cart edit request (plan §4 #15), only accepted while the order is NEW:
 * {@code add} inserts new lines, {@code update} changes quantities and
 * {@code remove} drops products (by product id) and returns their stock.
 * A product may appear in exactly one of the three lists.
 */
public record CartEditRequest(List<OrderItemRequest> add, List<OrderItemRequest> update, List<Integer> remove) {
}

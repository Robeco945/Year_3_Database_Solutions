package fi.metropolia.example.webstoreapi.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.Order} and its items.
 * Status is exposed as the enum's string form ('NEW', 'SHIPPED', 'DELIVERED',
 * 'CANCELLED'); customer-facing aggregates (order total, product names) come
 * from the view-backed endpoints in work-order step 3.
 */
public record OrderDto(Integer id, Integer customerId, LocalDateTime orderDate, LocalDateTime deliveryDate,
		Integer shippingAddressId, String status, List<OrderItemDto> items) {
}

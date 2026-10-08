package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Order item DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.OrderItem} (the N:M join
 * table row: which product, how many, at what unit price).
 */
public record OrderItemDto(Integer orderId, Integer productId, Integer quantity, BigDecimal unitPrice) {
}

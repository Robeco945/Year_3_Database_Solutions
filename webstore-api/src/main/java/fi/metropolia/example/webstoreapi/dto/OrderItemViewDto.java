package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * One order line in the order detail response (plan §4 #10): product name
 * joined in, quantity, unit price and line subtotal.
 */
public record OrderItemViewDto(Integer productId, String productName, Integer quantity, BigDecimal unitPrice,
		BigDecimal subtotal) {
}

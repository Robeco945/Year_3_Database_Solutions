package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order list/search row (plan §4 #9, #11, #12): order header + customer name
 * (joined from {@code customers}) + item count and total amount (aggregated
 * from {@code orderitems}).
 */
public record OrderSummaryDto(Integer id, LocalDateTime orderDate, String status, Integer customerId,
		String customerName, long itemCount, BigDecimal totalAmount) {
}

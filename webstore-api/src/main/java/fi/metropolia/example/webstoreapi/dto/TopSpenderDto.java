package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Top spender row (plan §4 #8): customer + order count + summed line totals,
 * produced with JOIN + GROUP BY + ORDER BY SUM.
 */
public record TopSpenderDto(Integer customerId, String customerName, String email, Long orderCount,
		BigDecimal totalSpent) {
}

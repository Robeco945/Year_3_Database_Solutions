package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface projection for the per-customer aggregates of plan §4 #6.
 * {@code totalSpent} / {@code lastOrderDate} are {@code null} for a customer
 * with no orders; the service normalizes them.
 */
public interface CustomerOrderStats {

	Long getOrderCount();

	BigDecimal getTotalSpent();

	LocalDateTime getLastOrderDate();

}

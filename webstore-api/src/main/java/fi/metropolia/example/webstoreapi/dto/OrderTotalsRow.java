package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface projection onto the {@code order_totals} view
 * ({@code db/views.sql}): one row per order, with the customer name and the
 * per-order aggregates coming straight from the view instead of being
 * assembled in the service. Used by the order list/search endpoints
 * (plan §4 #9/#11/#12).
 */
public interface OrderTotalsRow {

	Integer getOrderId();

	Integer getCustomerId();

	String getCustomerName();

	LocalDateTime getOrderDate();

	LocalDateTime getDeliveryDate();

	String getStatus();

	Long getItemCount();

	BigDecimal getTotalAmount();

}

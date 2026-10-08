package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Interface projection onto the {@code customer_summary} view for the
 * top-spenders ranking (plan §4 #8); the service maps it to
 * {@link TopSpenderDto}.
 */
public interface TopSpenderRow {

	Integer getCustomerId();

	String getCustomerName();

	String getEmail();

	Long getOrderCount();

	BigDecimal getTotalSpent();

}

package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Interface projection for item-count/total aggregates per order id, used when
 * composing {@link OrderSummaryDto}s for a page of orders.
 */
public interface OrderAggregate {

	Integer getOrderId();

	Long getItemCount();

	BigDecimal getTotalAmount();

}

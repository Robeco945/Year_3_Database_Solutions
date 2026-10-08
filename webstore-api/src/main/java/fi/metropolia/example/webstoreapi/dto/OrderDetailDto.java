package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order detail DTO (plan §4 #10): header + customer name + shipping address +
 * line items with product names/subtotals + order total.
 */
public record OrderDetailDto(Integer id, Integer customerId, String customerName, LocalDateTime orderDate,
		LocalDateTime deliveryDate, String status, CustomerAddressDto shippingAddress, List<OrderItemViewDto> items,
		BigDecimal totalAmount) {
}

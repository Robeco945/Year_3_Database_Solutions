package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer detail DTO (plan §4 #6): the customer combined with the contact
 * record (matched by email), delivery addresses and order-history aggregates
 * (order count, total spent, latest order date).
 */
public record CustomerDetailDto(Integer id, String firstName, String lastName, String email, String phone,
		ContactDto contact, List<CustomerAddressDto> addresses, long orderCount, BigDecimal totalSpent,
		LocalDateTime lastOrderDate) {
}

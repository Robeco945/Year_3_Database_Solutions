package fi.metropolia.example.webstoreapi.dto;

/**
 * Status transition request (plan §4 #14), e.g. {@code {"status":"SHIPPED"}}.
 * Allowed transitions: NEW → SHIPPED / CANCELLED, SHIPPED → DELIVERED.
 */
public record OrderStatusUpdateRequest(String status) {
}

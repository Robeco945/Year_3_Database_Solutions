package fi.metropolia.example.webstoreapi.dto;

/**
 * Request body for contact update (plan §4 #20). The reference is immutable,
 * so only the email can change.
 */
public record ContactUpdateRequest(String email) {
}

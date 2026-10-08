package fi.metropolia.example.webstoreapi.dto;

/**
 * Customer DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.Customer}. The aggregated
 * views (addresses + order history/total spent) arrive in work-order step 3 as
 * separate DTOs (planned endpoints #6).
 */
public record CustomerDto(Integer id, String firstName, String lastName, String email, String phone) {
}

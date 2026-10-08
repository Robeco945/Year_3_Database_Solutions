package fi.metropolia.example.webstoreapi.dto;

/**
 * Contact DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.Contact}.
 */
public record ContactDto(Integer id, String email, String reference) {
}

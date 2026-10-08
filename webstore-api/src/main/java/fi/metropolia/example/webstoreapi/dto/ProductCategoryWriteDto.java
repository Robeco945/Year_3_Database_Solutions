package fi.metropolia.example.webstoreapi.dto;

/**
 * Request body for product category create/update (plan §4 #18).
 */
public record ProductCategoryWriteDto(String name, String description) {
}

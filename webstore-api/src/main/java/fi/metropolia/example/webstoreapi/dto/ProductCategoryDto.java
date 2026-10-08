package fi.metropolia.example.webstoreapi.dto;

/**
 * ProductCategory DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.ProductCategory}.
 */
public record ProductCategoryDto(Integer id, String name, String description) {
}

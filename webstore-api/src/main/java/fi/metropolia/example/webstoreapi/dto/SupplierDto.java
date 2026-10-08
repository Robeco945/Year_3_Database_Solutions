package fi.metropolia.example.webstoreapi.dto;

/**
 * Supplier DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.Supplier}.
 */
public record SupplierDto(Integer id, String name, String contactName, String phone, String email) {
}

package fi.metropolia.example.webstoreapi.dto;

/**
 * Request body for supplier create/update (plan §2, admin view).
 */
public record SupplierWriteDto(String name, String contactName, String phone, String email) {
}

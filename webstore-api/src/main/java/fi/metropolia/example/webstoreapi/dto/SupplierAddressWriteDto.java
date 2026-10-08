package fi.metropolia.example.webstoreapi.dto;

/**
 * Request body for supplier address create/update (plan §2, admin view).
 */
public record SupplierAddressWriteDto(String streetAddress, String postalCode, String city, String country) {
}

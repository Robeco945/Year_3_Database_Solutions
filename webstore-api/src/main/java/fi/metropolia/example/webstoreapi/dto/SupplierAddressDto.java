package fi.metropolia.example.webstoreapi.dto;

/**
 * SupplierAddress DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.SupplierAddress}.
 */
public record SupplierAddressDto(Integer id, Integer supplierId, String streetAddress, String postalCode, String city,
		String country) {
}

package fi.metropolia.example.webstoreapi.dto;

/**
 * CustomerAddress DTO: API representation of
 * {@link fi.metropolia.example.webstoreapi.entity.CustomerAddress}.
 */
public record CustomerAddressDto(Integer id, Integer customerId, String streetAddress, String postalCode, String city,
		String country) {
}

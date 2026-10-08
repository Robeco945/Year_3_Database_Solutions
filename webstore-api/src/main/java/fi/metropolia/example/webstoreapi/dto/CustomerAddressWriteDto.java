package fi.metropolia.example.webstoreapi.dto;

/**
 * Request body for customer address create/update (plan §4 #19).
 */
public record CustomerAddressWriteDto(String streetAddress, String postalCode, String city, String country) {
}

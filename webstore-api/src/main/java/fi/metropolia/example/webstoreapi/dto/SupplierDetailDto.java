package fi.metropolia.example.webstoreapi.dto;

import java.util.List;

/**
 * Supplier detail DTO (plan §2, admin view): the supplier plus its 1:M
 * addresses.
 */
public record SupplierDetailDto(Integer id, String name, String contactName, String phone, String email,
		List<SupplierAddressDto> addresses) {
}

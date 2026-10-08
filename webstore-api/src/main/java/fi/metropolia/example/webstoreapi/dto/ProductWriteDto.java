package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Request body for admin product create/update (plan §4 #16). {@code type}
 * selects the subclass on create: P (base, default), PHYS or DIG; on update
 * the type is immutable.
 */
public record ProductWriteDto(String name, String description, BigDecimal price, Integer stockQuantity,
		Integer categoryId, Integer supplierId, String type, Integer weightGrams, String downloadUrl) {
}

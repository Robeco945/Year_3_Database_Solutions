package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Product detail DTO (plan §4 #2): the base product plus the polymorphic
 * subclass attributes of the JOINED inheritance hierarchy
 * ({@code type} = P / PHYS / DIG) and the optimistic locking {@code version}.
 */
public record ProductDetailDto(Integer id, String name, String description, BigDecimal price, Integer stockQuantity,
		Long version, String type, Integer weightGrams, String downloadUrl, Integer categoryId, String categoryName,
		Integer supplierId, String supplierName) {
}

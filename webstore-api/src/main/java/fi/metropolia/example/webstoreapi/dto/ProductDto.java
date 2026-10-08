package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Product DTO: API representation of {@link fi.metropolia.example.webstoreapi.entity.Product}.
 * The category/supplier ids and names are exposed so the catalogue view can
 * display classification info without a separate call (relations are EAGER on
 * the entity). Polymorphic subclass attributes (physical weight / digital
 * download) appear in the dedicated detail DTO in work-order step 3.
 */
public record ProductDto(Integer id, String name, String description, BigDecimal price, Integer stockQuantity,
		Integer categoryId, String categoryName, Integer supplierId, String supplierName) {
}

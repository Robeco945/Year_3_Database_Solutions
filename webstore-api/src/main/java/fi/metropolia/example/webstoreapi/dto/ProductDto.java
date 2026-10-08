package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Product DTO: API representation of {@link fi.metropolia.example.webstoreapi.entity.Product}.
 * The category/supplier ids are exposed so the catalogue view can display basic
 * classification info; full joined detail arrives in work-order step 3.
 */
public record ProductDto(Integer id, String name, String description, BigDecimal price, Integer stockQuantity,
		Integer categoryId, Integer supplierId) {
}

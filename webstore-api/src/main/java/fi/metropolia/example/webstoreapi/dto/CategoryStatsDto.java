package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Per-category product statistics (plan §4 #5): product count and price
 * aggregates produced with GROUP BY + HAVING.
 */
public record CategoryStatsDto(Integer categoryId, String categoryName, Long productCount, Double avgPrice,
		BigDecimal minPrice, BigDecimal maxPrice) {
}

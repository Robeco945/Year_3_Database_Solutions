package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Answer to "price of product X at time T" (plan §6 temporal feature).
 * {@code source} tells whether the answer comes from the versioned history
 * ({@code HISTORICAL}) or from the documented fallback for times before the
 * feature was deployed ({@code CURRENT_FALLBACK}; the current
 * {@code products.price}).
 */
public record ProductPriceAtDto(Integer productId, LocalDateTime at, BigDecimal price, String source) {

	public static final String SOURCE_HISTORICAL = "HISTORICAL";
	public static final String SOURCE_CURRENT_FALLBACK = "CURRENT_FALLBACK";
	public static final String SOURCE_CURRENT = "CURRENT";

}

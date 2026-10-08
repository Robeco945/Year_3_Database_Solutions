package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;

/**
 * Request body for the price update (plan §4 #17). The optional
 * {@code version} is the value read from the product detail: sending a stale
 * one fails with 409 (optimistic locking demo, plan §5).
 */
public record PriceUpdateRequest(BigDecimal price, Long version) {
}

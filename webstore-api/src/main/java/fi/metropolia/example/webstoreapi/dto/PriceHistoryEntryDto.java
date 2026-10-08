package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One entry of a product's price history (plan §6 temporal feature):
 * the price in effect from {@code changedAt} until {@code validUntil}
 * ({@code validUntil} is {@code null} — literally far-future in the versioned
 * table — for the current version).
 */
public record PriceHistoryEntryDto(BigDecimal price, LocalDateTime changedAt, LocalDateTime validUntil) {
}

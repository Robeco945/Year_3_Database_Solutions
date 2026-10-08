package fi.metropolia.example.webstoreapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Interface projection onto the system-versioned {@code productpricehistory}
 * table (temporal feature, plan §6): {@code ROW_START}/{@code ROW_END} are the
 * system-time bounds of {@code price}. {@code rowEnd} is far-future (year 2106)
 * for the current version.
 */
public interface PriceHistoryRow {

	BigDecimal getPrice();

	LocalDateTime getRowStart();

	LocalDateTime getRowEnd();

}

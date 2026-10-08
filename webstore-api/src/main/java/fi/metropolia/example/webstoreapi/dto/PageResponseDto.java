package fi.metropolia.example.webstoreapi.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable pagination envelope used by all list endpoints instead of serializing
 * Spring Data's internal {@code PageImpl} directly (its JSON shape is not a
 * stable API contract).
 */
public record PageResponseDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponseDto<T> of(Page<T> page) {
		return new PageResponseDto<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}

	/**
	 * Factory for queries paginated without {@link Page} (e.g. the native
	 * view-based order searches): total pages are derived from
	 * {@code totalElements}, rounding up.
	 */
	public static <T> PageResponseDto<T> of(List<T> content, int page, int size, long totalElements) {
		int totalPages = size <= 0 ? 0 : (int) Math.ceilDiv(totalElements, (long) size);
		return new PageResponseDto<>(content, page, size, totalElements, totalPages);
	}

}

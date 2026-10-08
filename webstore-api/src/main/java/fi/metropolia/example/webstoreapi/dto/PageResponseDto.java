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

}

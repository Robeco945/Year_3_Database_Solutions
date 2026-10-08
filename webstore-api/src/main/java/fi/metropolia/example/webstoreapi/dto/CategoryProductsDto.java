package fi.metropolia.example.webstoreapi.dto;

/**
 * Products of one category together with the category's own data (plan §4 #4).
 */
public record CategoryProductsDto(ProductCategoryDto category, PageResponseDto<ProductDto> products) {
}

package fi.metropolia.example.webstoreapi.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.CategoryProductsDto;
import fi.metropolia.example.webstoreapi.dto.CategoryStatsDto;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.PriceHistoryEntryDto;
import fi.metropolia.example.webstoreapi.dto.PriceUpdateRequest;
import fi.metropolia.example.webstoreapi.dto.ProductDetailDto;
import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.dto.ProductPriceAtDto;
import fi.metropolia.example.webstoreapi.dto.ProductWriteDto;
import fi.metropolia.example.webstoreapi.service.ProductService;

/**
 * Product endpoints: catalogue browsing for the webshop view, admin
 * create/update (plan §4 #1–#5, #16, #17).
 */
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public PageResponseDto<ProductDto> listProducts(
			@RequestParam(name = "category", required = false) Integer categoryId,
			@RequestParam(name = "supplier", required = false) Integer supplierId,
			@RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
			@RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
			@RequestParam(name = "search", required = false) String search,
			@PageableDefault(size = 20, sort = "name") Pageable pageable) {
		return productService.listProducts(categoryId, supplierId, minPrice, maxPrice, search, pageable);
	}

	@GetMapping("/{id}")
	public ProductDetailDto getProduct(@PathVariable("id") Integer id) {
		return productService.getProduct(id);
	}

	@GetMapping("/price/{min}")
	public List<ProductDto> getProductsByMinPrice(@PathVariable("min") BigDecimal min) {
		return productService.findByMinPrice(min);
	}

	/**
	 * Temporal feature (plan §6): full price change log of one product, oldest
	 * first (system-versioned {@code productpricehistory}).
	 */
	@GetMapping("/{id}/price-history")
	public List<PriceHistoryEntryDto> getPriceHistory(@PathVariable("id") Integer id) {
		return productService.priceHistory(id);
	}

	/**
	 * Temporal feature (plan §6): "price at time T". Optional ISO date-time
	 * {@code at}; omitted → current price. Times before the feature's
	 * deployment fall back to the current price and are marked as such
	 * ({@code source}).
	 */
	@GetMapping("/{id}/price")
	public ProductPriceAtDto getPriceAt(@PathVariable("id") Integer id,
			@RequestParam(name = "at", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at) {
		return productService.priceAtTime(id, at);
	}

	@GetMapping("/category/{id}")
	public CategoryProductsDto getProductsByCategory(@PathVariable("id") Integer id,
			@PageableDefault(size = 20, sort = "name") Pageable pageable) {
		return productService.getProductsByCategory(id, pageable);
	}

	@GetMapping("/stats/by-category")
	public List<CategoryStatsDto> getCategoryStats(
			@RequestParam(name = "minProducts", defaultValue = "1") long minProducts) {
		return productService.categoryStats(minProducts);
	}

	@PostMapping
	public ResponseEntity<ProductDetailDto> createProduct(@RequestBody ProductWriteDto dto) {
		ProductDetailDto created = productService.createProduct(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ProductDetailDto updateProduct(@PathVariable("id") Integer id, @RequestBody ProductWriteDto dto) {
		return productService.updateProduct(id, dto);
	}

	@PutMapping("/{id}/price")
	public ProductDetailDto updatePrice(@PathVariable("id") Integer id, @RequestBody PriceUpdateRequest request) {
		return productService.updatePrice(id, request);
	}

}

package fi.metropolia.example.webstoreapi.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.ProductCategoryDto;
import fi.metropolia.example.webstoreapi.dto.ProductCategoryWriteDto;
import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.service.ProductCategoryService;
import fi.metropolia.example.webstoreapi.service.ProductService;

/**
 * Product category CRUD (plan §4 #18) plus the paged product listing of one
 * category (#4). The path is more specific than {@code /products/{id}} so both
 * coexist under /products.
 */
@RestController
@RequestMapping("/products/categories")
public class ProductCategoryController {

	private final ProductCategoryService categoryService;
	private final ProductService productService;

	public ProductCategoryController(ProductCategoryService categoryService, ProductService productService) {
		this.categoryService = categoryService;
		this.productService = productService;
	}

	@GetMapping
	public List<ProductCategoryDto> listCategories() {
		return categoryService.listCategories();
	}

	@GetMapping("/{id}")
	public ProductCategoryDto getCategory(@PathVariable("id") Integer id) {
		return categoryService.getCategory(id);
	}

	@GetMapping("/{id}/products")
	public PageResponseDto<ProductDto> getCategoryProducts(@PathVariable("id") Integer id,
			@PageableDefault(size = 20, sort = "name") Pageable pageable) {
		return productService.getProductsByCategory(id, pageable).products();
	}

	@PostMapping
	public ResponseEntity<ProductCategoryDto> createCategory(@RequestBody ProductCategoryWriteDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(dto));
	}

	@PutMapping("/{id}")
	public ProductCategoryDto updateCategory(@PathVariable("id") Integer id, @RequestBody ProductCategoryWriteDto dto) {
		return categoryService.updateCategory(id, dto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable("id") Integer id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}

}

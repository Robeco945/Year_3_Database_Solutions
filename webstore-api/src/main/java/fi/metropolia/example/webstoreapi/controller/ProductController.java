package fi.metropolia.example.webstoreapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;

/**
 * Product endpoints for the webshop customer view.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

	private final ProductRepository productRepository;

	public ProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductDto> getProduct(@PathVariable("id") Integer id) {
		return productRepository.findById(id)
				.map(p -> ResponseEntity.ok(new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
						p.getStockQuantity(),
						p.getCategory() != null ? p.getCategory().getId() : null,
						p.getCategory() != null ? p.getCategory().getName() : null,
						p.getSupplier() != null ? p.getSupplier().getId() : null,
						p.getSupplier() != null ? p.getSupplier().getName() : null)))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
	}

}

package fi.metropolia.example.webstoreapi.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.entity.Product;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;

/**
 * Product endpooints for the webshop customer view.
 *
 * <p>Scaffold endpoint (work-order step 1): GET /products/{id} — products
 * catalogue read with the DB user's least-privilege SELECT grant on products.</p>
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
		Optional<Product> product = productRepository.findById(id);
		return product.map(p -> ResponseEntity.ok(new ProductDto(p.getId(), p.getName(), p.getDescription(),
				p.getPrice(), p.getStockQuantity(), p.getCategoryId(), p.getSupplierId())))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
	}

}

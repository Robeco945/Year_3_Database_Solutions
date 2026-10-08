package fi.metropolia.example.webstoreapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.CategoryProductsDto;
import fi.metropolia.example.webstoreapi.dto.CategoryStatsDto;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.PriceUpdateRequest;
import fi.metropolia.example.webstoreapi.dto.ProductCategoryDto;
import fi.metropolia.example.webstoreapi.dto.ProductDetailDto;
import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.dto.ProductWriteDto;
import fi.metropolia.example.webstoreapi.entity.DigitalProduct;
import fi.metropolia.example.webstoreapi.entity.PhysicalProduct;
import fi.metropolia.example.webstoreapi.entity.Product;
import fi.metropolia.example.webstoreapi.entity.ProductCategory;
import fi.metropolia.example.webstoreapi.entity.Supplier;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.ProductCategoryRepository;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;
import fi.metropolia.example.webstoreapi.repository.SupplierRepository;

/**
 * Product catalogue and admin product management (plan §2, §4 #1–#5, #16, #17).
 */
@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductCategoryRepository categoryRepository;
	private final SupplierRepository supplierRepository;

	public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository,
			SupplierRepository supplierRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.supplierRepository = supplierRepository;
	}

	@Transactional(readOnly = true)
	public PageResponseDto<ProductDto> listProducts(Integer categoryId, Integer supplierId, BigDecimal minPrice,
			BigDecimal maxPrice, String search, Pageable pageable) {
		Page<ProductDto> page = productRepository.searchProducts(categoryId, supplierId, minPrice, maxPrice,
				normalizeSearch(search), pageable);
		return PageResponseDto.of(page);
	}

	@Transactional(readOnly = true)
	public ProductDetailDto getProduct(Integer id) {
		return toDetail(findProduct(id));
	}

	/** Single-table derived query (plan §4 #3). */
	@Transactional(readOnly = true)
	public List<ProductDto> findByMinPrice(BigDecimal minPrice) {
		return productRepository.findByPriceGreaterThanEqualOrderByPriceAsc(minPrice).stream().map(this::toDto).toList();
	}

	/** Products of one category with the category's own data (plan §4 #4). */
	@Transactional(readOnly = true)
	public CategoryProductsDto getProductsByCategory(Integer categoryId, Pageable pageable) {
		ProductCategory category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found: id=" + categoryId));
		Page<ProductDto> products = productRepository.searchProducts(categoryId, null, null, null, null, pageable);
		return new CategoryProductsDto(toCategoryDto(category), PageResponseDto.of(products));
	}

	/** GROUP BY + HAVING + aggregates (plan §4 #5). */
	@Transactional(readOnly = true)
	public List<CategoryStatsDto> categoryStats(long minProducts) {
		if (minProducts < 0) {
			throw new IllegalArgumentException("minProducts must be >= 0");
		}
		return productRepository.findCategoryStats(minProducts);
	}

	/** Admin create (plan §4 #16); {@code type} picks the subclass on create. */
	@Transactional
	public ProductDetailDto createProduct(ProductWriteDto dto) {
		validateProduct(dto);
		Product product = instantiate(dto.type());
		applyBaseFields(product, dto);
		if (product instanceof PhysicalProduct physical) {
			physical.setWeightGrams(dto.weightGrams());
		}
		else if (product instanceof DigitalProduct digital) {
			digital.setDownloadUrl(dto.downloadUrl());
		}
		return toDetail(productRepository.save(product));
	}

	/** Admin update (plan §4 #16); the discriminator/type cannot change. */
	@Transactional
	public ProductDetailDto updateProduct(Integer id, ProductWriteDto dto) {
		Product product = findProduct(id);
		validateProduct(dto);
		if (dto.type() != null && !dto.type().isBlank()
				&& !typeOf(product).equals(dto.type().trim().toUpperCase(Locale.ROOT))) {
			throw new IllegalArgumentException("Product type cannot be changed (is " + typeOf(product) + ")");
		}
		applyBaseFields(product, dto);
		if (product instanceof PhysicalProduct physical && dto.weightGrams() != null) {
			physical.setWeightGrams(dto.weightGrams());
		}
		else if (product instanceof DigitalProduct digital && dto.downloadUrl() != null) {
			digital.setDownloadUrl(dto.downloadUrl());
		}
		return toDetail(productRepository.saveAndFlush(product));
	}

	/**
	 * Price change (plan §4 #17). A stale {@code version} sent by the client is
	 * rejected with 409; concurrent transactions are additionally protected by
	 * the entity's automatic {@code @Version} check (plan §5).
	 */
	@Transactional
	public ProductDetailDto updatePrice(Integer id, PriceUpdateRequest request) {
		Product product = findProduct(id);
		if (request == null || request.price() == null || request.price().signum() < 0) {
			throw new IllegalArgumentException("price is required and must be >= 0");
		}
		if (request.version() != null && !request.version().equals(product.getVersion())) {
			throw new OptimisticLockingFailureException("Product " + id + " was modified concurrently (current version "
					+ product.getVersion() + ", sent " + request.version() + ")");
		}
		product.setPrice(request.price());
		return toDetail(productRepository.saveAndFlush(product));
	}

	private Product findProduct(Integer id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));
	}

	private Product instantiate(String type) {
		String normalized = type == null || type.isBlank() ? "P" : type.trim().toUpperCase(Locale.ROOT);
		return switch (normalized) {
			case "P" -> new Product();
			case "PHYS" -> new PhysicalProduct();
			case "DIG" -> new DigitalProduct();
			default -> throw new IllegalArgumentException("Unknown product type '" + type + "'; expected P, PHYS or DIG");
		};
	}

	private String typeOf(Product product) {
		if (product instanceof PhysicalProduct) {
			return "PHYS";
		}
		if (product instanceof DigitalProduct) {
			return "DIG";
		}
		return "P";
	}

	private void validateProduct(ProductWriteDto dto) {
		if (dto == null || dto.name() == null || dto.name().isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
		if (dto.price() == null || dto.price().signum() < 0) {
			throw new IllegalArgumentException("price is required and must be >= 0");
		}
		if (dto.stockQuantity() == null || dto.stockQuantity() < 0) {
			throw new IllegalArgumentException("stockQuantity is required and must be >= 0");
		}
	}

	private void applyBaseFields(Product product, ProductWriteDto dto) {
		product.setName(dto.name().trim());
		product.setDescription(dto.description());
		product.setPrice(dto.price());
		product.setStockQuantity(dto.stockQuantity());
		product.setCategory(resolveCategory(dto.categoryId()));
		product.setSupplier(resolveSupplier(dto.supplierId()));
	}

	private ProductCategory resolveCategory(Integer categoryId) {
		if (categoryId == null) {
			return null;
		}
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found: id=" + categoryId));
	}

	private Supplier resolveSupplier(Integer supplierId) {
		if (supplierId == null) {
			return null;
		}
		return supplierRepository.findById(supplierId)
				.orElseThrow(() -> new ResourceNotFoundException("Supplier not found: id=" + supplierId));
	}

	private String normalizeSearch(String search) {
		return search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
	}

	private ProductDto toDto(Product product) {
		return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getStockQuantity(),
				product.getCategory() != null ? product.getCategory().getId() : null,
				product.getCategory() != null ? product.getCategory().getName() : null,
				product.getSupplier() != null ? product.getSupplier().getId() : null,
				product.getSupplier() != null ? product.getSupplier().getName() : null);
	}

	private ProductCategoryDto toCategoryDto(ProductCategory category) {
		return new ProductCategoryDto(category.getId(), category.getName(), category.getDescription());
	}

	private ProductDetailDto toDetail(Product product) {
		Integer weightGrams = product instanceof PhysicalProduct physical ? physical.getWeightGrams() : null;
		String downloadUrl = product instanceof DigitalProduct digital ? digital.getDownloadUrl() : null;
		return new ProductDetailDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
				product.getStockQuantity(), product.getVersion(), typeOf(product), weightGrams, downloadUrl,
				product.getCategory() != null ? product.getCategory().getId() : null,
				product.getCategory() != null ? product.getCategory().getName() : null,
				product.getSupplier() != null ? product.getSupplier().getId() : null,
				product.getSupplier() != null ? product.getSupplier().getName() : null);
	}

}

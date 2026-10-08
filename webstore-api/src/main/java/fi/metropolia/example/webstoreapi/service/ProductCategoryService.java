package fi.metropolia.example.webstoreapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.ProductCategoryDto;
import fi.metropolia.example.webstoreapi.dto.ProductCategoryWriteDto;
import fi.metropolia.example.webstoreapi.entity.ProductCategory;
import fi.metropolia.example.webstoreapi.exception.ConflictException;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.ProductCategoryRepository;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;

/**
 * Product category CRUD (plan §2, §4 #18) — the small, fully manageable
 * catalogue lookup table.
 */
@Service
public class ProductCategoryService {

	private final ProductCategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	public ProductCategoryService(ProductCategoryRepository categoryRepository, ProductRepository productRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public List<ProductCategoryDto> listCategories() {
		return categoryRepository.findAll().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ProductCategoryDto getCategory(Integer id) {
		return toDto(findCategory(id));
	}

	@Transactional
	public ProductCategoryDto createCategory(ProductCategoryWriteDto dto) {
		validate(dto);
		ProductCategory category = new ProductCategory();
		category.setName(dto.name().trim());
		category.setDescription(dto.description());
		return toDto(categoryRepository.save(category));
	}

	@Transactional
	public ProductCategoryDto updateCategory(Integer id, ProductCategoryWriteDto dto) {
		ProductCategory category = findCategory(id);
		validate(dto);
		category.setName(dto.name().trim());
		category.setDescription(dto.description());
		return toDto(categoryRepository.save(category));
	}

	@Transactional
	public void deleteCategory(Integer id) {
		ProductCategory category = findCategory(id);
		if (productRepository.countByCategoryId(id) > 0) {
			throw new ConflictException(
					"Category " + id + " still has products and cannot be deleted (FK productcategories)");
		}
		categoryRepository.delete(category);
	}

	private ProductCategory findCategory(Integer id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found: id=" + id));
	}

	private void validate(ProductCategoryWriteDto dto) {
		if (dto == null || dto.name() == null || dto.name().isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
	}

	private ProductCategoryDto toDto(ProductCategory category) {
		return new ProductCategoryDto(category.getId(), category.getName(), category.getDescription());
	}

}

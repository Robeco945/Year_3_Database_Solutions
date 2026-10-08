package fi.metropolia.example.webstoreapi.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.dto.CategoryStatsDto;
import fi.metropolia.example.webstoreapi.dto.ProductDto;
import fi.metropolia.example.webstoreapi.entity.Product;
import jakarta.persistence.LockModeType;

/**
 * Spring Data repository for {@link Product}.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so the later filtered
 * catalogue endpoint (work-order step 3) can use dynamic {@link org.springframework.data.jpa.domain.Specification}
 * queries without further repository changes.</p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

	@Override
	Optional<Product> findById(Integer id);

	/**
	 * Filtered catalogue listing (plan §4 #1): one projection query joins the
	 * category and supplier names in, so no N+1 lazy/eager loads occur. A
	 * {@code null} filter value is ignored; pagination + sorting come from
	 * Spring Data.
	 */
	@Query(value = """
			SELECT new fi.metropolia.example.webstoreapi.dto.ProductDto(
				p.id, p.name, p.description, p.price, p.stockQuantity,
				c.id, c.name, s.id, s.name)
			FROM Product p
			LEFT JOIN p.category c
			LEFT JOIN p.supplier s
			WHERE (:categoryId IS NULL OR c.id = :categoryId)
				AND (:supplierId IS NULL OR s.id = :supplierId)
				AND (:minPrice IS NULL OR p.price >= :minPrice)
				AND (:maxPrice IS NULL OR p.price <= :maxPrice)
				AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
			""", countQuery = """
			SELECT COUNT(p)
			FROM Product p
			LEFT JOIN p.category c
			LEFT JOIN p.supplier s
			WHERE (:categoryId IS NULL OR c.id = :categoryId)
				AND (:supplierId IS NULL OR s.id = :supplierId)
				AND (:minPrice IS NULL OR p.price >= :minPrice)
				AND (:maxPrice IS NULL OR p.price <= :maxPrice)
				AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
			""")
	Page<ProductDto> searchProducts(@Param("categoryId") Integer categoryId, @Param("supplierId") Integer supplierId,
			@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
			@Param("search") String search, Pageable pageable);

	/** Simple single-table derived query (plan §4 #3). */
	List<Product> findByPriceGreaterThanEqualOrderByPriceAsc(BigDecimal minPrice);

	/**
	 * Pessimistic row locks ({@code SELECT ... FOR UPDATE}) on the given
	 * products — used by checkout and cart editing so concurrent transactions
	 * cannot oversell the same stock (plan §5). Must run inside a transaction.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id IN :ids")
	List<Product> findAllByIdForUpdate(@Param("ids") Collection<Integer> ids);

	/**
	 * Per-category product statistics (plan §4 #5): GROUP BY + HAVING +
	 * aggregate functions; only categories with at least {@code minProducts}
	 * products are returned.
	 */
	@Query("""
			SELECT new fi.metropolia.example.webstoreapi.dto.CategoryStatsDto(
				pc.id, pc.name, COUNT(p), AVG(p.price), MIN(p.price), MAX(p.price))
			FROM Product p
			JOIN p.category pc
			GROUP BY pc.id, pc.name
			HAVING COUNT(p) >= :minProducts
			ORDER BY COUNT(p) DESC
			""")
	List<CategoryStatsDto> findCategoryStats(@Param("minProducts") long minProducts);

	long countByCategoryId(Integer categoryId);

	long countBySupplierId(Integer supplierId);

}

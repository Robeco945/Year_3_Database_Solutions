package fi.metropolia.example.webstoreapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.ProductCategory;

/**
 * Spring Data repository for {@link ProductCategory}.
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {

}

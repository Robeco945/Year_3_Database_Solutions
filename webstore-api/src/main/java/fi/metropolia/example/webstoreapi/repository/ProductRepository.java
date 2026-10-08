package fi.metropolia.example.webstoreapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Product;

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
}

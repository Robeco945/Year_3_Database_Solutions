package fi.metropolia.example.webstoreapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Supplier;

/**
 * Spring Data repository for {@link Supplier}.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

	Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);

}

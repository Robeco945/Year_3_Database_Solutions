package fi.metropolia.example.webstoreapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.SupplierAddress;

/**
 * Spring Data repository for {@link SupplierAddress}.
 */
@Repository
public interface SupplierAddressRepository extends JpaRepository<SupplierAddress, Integer> {

	List<SupplierAddress> findBySupplierId(Integer supplierId);

}

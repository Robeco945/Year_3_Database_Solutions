package fi.metropolia.example.webstoreapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Customer;

/**
 * Spring Data repository for {@link Customer} (read-heavy resource: plan §2).
 * JpaSpecificationExecutor supports later multi-criteria customer queries.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {

}

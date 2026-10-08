package fi.metropolia.example.webstoreapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Customer;

/**
 * Spring Data repository for {@link Customer} (read-heavy resource: plan §2).
 * JpaSpecificationExecutor supports later multi-criteria customer queries.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {

	/**
	 * Customers missing from {@code orders} — NOT EXISTS subquery (plan §4 #7).
	 */
	@Query(value = "SELECT c FROM Customer c WHERE NOT EXISTS (SELECT 1 FROM Order o WHERE o.customer = c)",
			countQuery = "SELECT COUNT(c) FROM Customer c WHERE NOT EXISTS (SELECT 1 FROM Order o WHERE o.customer = c)")
	Page<Customer> findCustomersWithoutOrders(Pageable pageable);

}

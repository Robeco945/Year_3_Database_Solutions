package fi.metropolia.example.webstoreapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.CustomerAddress;

/**
 * Spring Data repository for {@link CustomerAddress}.
 * Derived customer-id lookup is the hot query of the address management flow;
 * index on customeraddresses.customer_id is created via db/indexes.sql (§6).
 */
@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

	List<CustomerAddress> findByCustomerId(Integer customerId);

}

package fi.metropolia.example.webstoreapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.entity.Order;

/**
 * Spring Data repository for {@link Order}.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

	/**
	 * Order detail with items — explicit JOIN FETCH avoids the N+1 query problem
	 * when reading one order with all its items (line items come from the 1:M
	 * Order → OrderItems collection; plan §6b).
	 */
	@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
	Optional<Order> findWithItemsById(Integer id);

}

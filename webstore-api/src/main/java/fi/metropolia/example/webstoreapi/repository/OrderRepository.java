package fi.metropolia.example.webstoreapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.dto.CustomerOrderStats;
import fi.metropolia.example.webstoreapi.dto.TopSpenderDto;
import fi.metropolia.example.webstoreapi.entity.Order;

/**
 * Spring Data repository for {@link Order}.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

	/**
	 * Order detail with items and their products — explicit JOIN FETCH avoids
	 * the N+1 query problem when reading one order with all its lines (plan §6b).
	 */
	@Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
	Optional<Order> findWithItemsById(Integer id);

	/**
	 * Per-customer order count, total spend and latest order date, combining
	 * {@code orders} + {@code orderitems} (plan §4 #6).
	 */
	@Query("""
			SELECT COUNT(DISTINCT o.id) AS orderCount,
			       SUM(i.quantity * i.unitPrice) AS totalSpent,
			       MAX(o.orderDate) AS lastOrderDate
			FROM Order o
			LEFT JOIN o.items i
			WHERE o.customer.id = :customerId
			""")
	CustomerOrderStats findCustomerOrderStats(@Param("customerId") Integer customerId);

	/**
	 * Top spenders (plan §4 #8): JOIN customers-orders-orderitems, GROUP BY
	 * customer, ORDER BY the summed line totals DESC.
	 */
	@Query("""
			SELECT new fi.metropolia.example.webstoreapi.dto.TopSpenderDto(
				c.id, CONCAT(c.firstName, ' ', c.lastName), c.email,
				COUNT(DISTINCT o.id), SUM(i.quantity * i.unitPrice))
			FROM Customer c
			JOIN c.orders o
			JOIN o.items i
			GROUP BY c.id, c.firstName, c.lastName, c.email
			ORDER BY SUM(i.quantity * i.unitPrice) DESC
			""")
	List<TopSpenderDto> findTopSpenders(Pageable pageable);

	/** An address referenced as a shipping address must not be deleted (plan §2). */
	boolean existsByShippingAddressId(Integer shippingAddressId);

}

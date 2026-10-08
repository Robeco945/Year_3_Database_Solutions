package fi.metropolia.example.webstoreapi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.dto.CustomerOrderStats;
import fi.metropolia.example.webstoreapi.dto.OrderTotalsRow;
import fi.metropolia.example.webstoreapi.dto.TopSpenderRow;
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
	 * Per-customer order count, total spend and latest order date — served by
	 * the {@code customer_summary} view ({@code db/views.sql}, plan §4 #6).
	 * {@code total_spent} excludes CANCELLED orders (view semantics;
	 * documented in README).
	 */
	@Query(nativeQuery = true, value = """
			SELECT order_count      AS orderCount,
			       total_spent      AS totalSpent,
			       latest_order_date AS lastOrderDate
			FROM customer_summary
			WHERE customer_id = :customerId
			""")
	CustomerOrderStats findCustomerOrderStats(@Param("customerId") Integer customerId);

	/**
	 * Top spenders (plan §4 #8) — ranked from the {@code customer_summary}
	 * view (ORDER BY total_spent DESC); joined to customers only for the email
	 * column. Requires at least one order.
	 */
	@Query(nativeQuery = true, value = """
			SELECT cs.customer_id    AS customerId,
			       cs.customer_name  AS customerName,
			       c.email           AS email,
			       cs.order_count    AS orderCount,
			       cs.total_spent    AS totalSpent
			FROM customer_summary cs
			JOIN customers c ON c.id = cs.customer_id
			WHERE cs.order_count > 0
			ORDER BY cs.total_spent DESC
			LIMIT :limit
			""")
	List<TopSpenderRow> findTopSpenders(@Param("limit") int limit);

	/**
	 * Order list/search (plan §4 #9/#11/#12) — served by the
	 * {@code order_totals} view: the page's customer names and per-order
	 * aggregates come from the view in one query, newest first.
	 * Filters are all optional (NULL = ignored).
	 */
	@Query(nativeQuery = true, value = """
			SELECT order_id      AS orderId,
			       customer_id   AS customerId,
			       customer_name AS customerName,
			       order_date    AS orderDate,
			       delivery_date AS deliveryDate,
			       status        AS status,
			       item_count    AS itemCount,
			       total_amount  AS totalAmount
			FROM order_totals
			WHERE (:statusName IS NULL OR status = :statusName)
			  AND (:customerId IS NULL OR customer_id = :customerId)
			  AND (:dateFrom IS NULL OR order_date >= :dateFrom)
			  AND (:dateTo   IS NULL OR order_date <= :dateTo)
			ORDER BY order_date DESC, order_id DESC
			LIMIT :limit OFFSET :offset
			""")
	List<OrderTotalsRow> findOrderTotals(@Param("statusName") String statusName,
			@Param("customerId") Integer customerId, @Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo, @Param("limit") int limit, @Param("offset") int offset);

	/** Count query matching {@link #findOrderTotals} for the pagination envelope. */
	@Query(nativeQuery = true, value = """
			SELECT COUNT(*)
			FROM order_totals
			WHERE (:statusName IS NULL OR status = :statusName)
			  AND (:customerId IS NULL OR customer_id = :customerId)
			  AND (:dateFrom IS NULL OR order_date >= :dateFrom)
			  AND (:dateTo   IS NULL OR order_date <= :dateTo)
			""")
	long countOrderTotals(@Param("statusName") String statusName, @Param("customerId") Integer customerId,
			@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

	/** An address referenced as a shipping address must not be deleted (plan §2). */
	boolean existsByShippingAddressId(Integer shippingAddressId);

}

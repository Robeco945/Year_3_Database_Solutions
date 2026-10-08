package fi.metropolia.example.webstoreapi.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.metropolia.example.webstoreapi.dto.OrderAggregate;
import fi.metropolia.example.webstoreapi.entity.OrderItem;
import fi.metropolia.example.webstoreapi.entity.OrderItemId;

/**
 * Spring Data repository for {@link OrderItem} (the Orders ↔ Products join
 * table with attributes; see OrderItem javadoc).
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

	List<OrderItem> findByOrderId(Integer orderId);

	List<OrderItem> findByProductId(Integer productId);

	/**
	 * Item count + summed total per order, for the order list/search DTOs —
	 * one aggregate query per page instead of touching every order's items.
	 */
	@Query("""
			SELECT i.orderId AS orderId, COUNT(i) AS itemCount, SUM(i.quantity * i.unitPrice) AS totalAmount
			FROM OrderItem i
			WHERE i.orderId IN :orderIds
			GROUP BY i.orderId
			""")
	List<OrderAggregate> aggregateForOrders(@Param("orderIds") Collection<Integer> orderIds);

}

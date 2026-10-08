package fi.metropolia.example.webstoreapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}

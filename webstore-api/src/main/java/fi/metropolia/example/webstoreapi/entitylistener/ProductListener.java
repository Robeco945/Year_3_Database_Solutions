package fi.metropolia.example.webstoreapi.entitylistener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.metropolia.example.webstoreapi.entity.Product;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreUpdate;

/**
 * JPA entity listener for {@link Product} (mirrors HaltijaListener).
 *
 * <p>Demonstrates lifecycle callbacks; the DB-level change history (price log
 * with triggers) is a separate feature (PROJECT_PLAN.md §6). The listener
 * simply writes an application log entry for audit purposes.</p>
 */
public class ProductListener {

	private static final Logger log = LoggerFactory.getLogger(ProductListener.class);

	@PostPersist
	public void afterPersist(Product product) {
		log.info("@PostPersist: product saved, id={}, name={}, price={}", product.getId(), product.getName(),
				product.getPrice());
	}

	@PreUpdate
	public void beforeUpdate(Product product) {
		log.info("@PreUpdate: product update, id={}, price={}, stockQuantity={}", product.getId(), product.getPrice(),
				product.getStockQuantity());
	}

}

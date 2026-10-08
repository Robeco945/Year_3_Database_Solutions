package fi.metropolia.example.webstoreapi.entity;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.util.ProxyUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Product entity, mapped to the webstore "products" table.
 *
 * <p>One entity suffices for the scaffold endpoint (GET /products/{id});
 * the relations to {@code productcategories} and {@code suppliers} are added in
 * work-order step 2 (domain layer).</p>
 */
@Entity
@Table(name = "products", indexes = {
		@Index(name = "ix_products_category", columnList = "category_id"),
		@Index(name = "ix_products_supplier", columnList = "supplier_id") })
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@NotBlank
	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description", length = 65535)
	private String description;

	@NotNull
	@DecimalMin("0.00")
	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@NotNull
	@PositiveOrZero
	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@Column(name = "category_id", updatable = false, insertable = false)
	private Integer categoryId;

	@Column(name = "supplier_id", updatable = false, insertable = false)
	private Integer supplierId;

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public Integer getStockQuantity() {
		return this.stockQuantity;
	}

	public Integer getCategoryId() {
		return this.categoryId;
	}

	public Integer getSupplierId() {
		return this.supplierId;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "("
				+ (id != null ? "id=" + id + ", " : "")
				+ "name=" + name + ", "
				+ "price=" + price + ", "
				+ "stockQuantity=" + stockQuantity + ")";
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Product entity)) {
			return false;
		}
		return this.id != null && this.id.equals(entity.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}

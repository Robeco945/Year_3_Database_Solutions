package fi.metropolia.example.webstoreapi.entity;

import java.math.BigDecimal;

import org.springframework.lang.Nullable;

import fi.metropolia.example.webstoreapi.entitylistener.ProductListener;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Product, mapped to the webstore {@code products} table.
 *
 * <p><b>Inheritance:</b> root of a {@link InheritanceType#JOINED} hierarchy with
 * the subclasses {@link PhysicalProduct} and {@link DigitalProduct} (see
 * {@code db/product_subtypes.sql}; the schema change is documented in the
 * README). Base-class products use discriminator value {@code P}.</p>
 *
 * <p><b>Loading:</b> {@code @ManyToOne} relations to category/supplier are
 * EAGER — a product without its category/supplier context is not a sensible
 * response for the webshop view, and both are small lookups. Lists solve the
 * N+1 issue with JOIN FETCH queries in repository methods (documented).</p>
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING, length = 31)
@DiscriminatorValue("P")
@EntityListeners(ProductListener.class)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description", length = 65535)
	private String description;

	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "category_id")
	@Nullable
	private ProductCategory category;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "supplier_id")
	@Nullable
	private Supplier supplier;

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
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

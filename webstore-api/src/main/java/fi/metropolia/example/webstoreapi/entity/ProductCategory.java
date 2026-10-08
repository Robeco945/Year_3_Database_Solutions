package fi.metropolia.example.webstoreapi.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Product category, mapped to {@code productcategories}.
 *
 * <p>Products collection is LAZY: categories are read far more often than the
 * full product list of each category.</p>
 */
@Entity
@Table(name = "productcategories")
public class ProductCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "description", length = 65535)
	private String description;

	@OneToMany(mappedBy = "category")
	private List<Product> products = new ArrayList<>();

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

	public List<Product> getProducts() {
		return products;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(id=" + id + ", name=" + name + ")";
	}

}

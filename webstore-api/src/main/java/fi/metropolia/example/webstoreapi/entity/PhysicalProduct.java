package fi.metropolia.example.webstoreapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * Physical product: a {@link Product} subclass. Holds subclass-specific
 * attributes in its own joined table ({@code physicalproducts}).
 */
@Entity
@Table(name = "physicalproducts")
@PrimaryKeyJoinColumn(name = "product_id", referencedColumnName = "id")
@DiscriminatorValue("PHYS")
public class PhysicalProduct extends Product {

	@Column(name = "weight_grams")
	private Integer weightGrams;

	public Integer getWeightGrams() {
		return weightGrams;
	}

	public void setWeightGrams(Integer weightGrams) {
		this.weightGrams = weightGrams;
	}

}

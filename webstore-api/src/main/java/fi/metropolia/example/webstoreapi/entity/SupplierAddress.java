package fi.metropolia.example.webstoreapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Supplier address, mapped to {@code supplieraddresses} (1:M to Supplier).
 * The owning side of the relation (FK {@code supplier_id} lives here).
 */
@Entity
@Table(name = "supplieraddresses")
public class SupplierAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "supplier_id", nullable = false)
	private Supplier supplier;

	@Column(name = "street_address", nullable = false, length = 255)
	private String streetAddress;

	@Column(name = "postal_code", length = 20)
	private String postalCode;

	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@Column(name = "country", length = 100)
	private String country;

	public Integer getId() {
		return id;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(id=" + id + ", city=" + city + ")";
	}

}

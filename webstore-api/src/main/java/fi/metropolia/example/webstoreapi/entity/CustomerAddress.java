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
 * Customer delivery address, mapped to {@code customeraddresses} (1:M to
 * Customer; FK {@code customer_id} lives here → owning side of the relation).
 *
 * <p>The FK {@code orders.shipping_address_id} also references this table —
 * the persistence side of the Order↔CustomerAddress 1:1 navigable association
 * lives in {@link Order}. A customer address referenced as a shipping address
 * must not be deleted; delegated to the FK constraint / service rules.</p>
 */
@Entity
@Table(name = "customeraddresses")
public class CustomerAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

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

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
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

}

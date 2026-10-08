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
 * Supplier, mapped to {@code suppliers}. Owns its 1:M collections of addresses
 * and offered products (both LAZY, loaded by dedicated queries).
 */
@Entity
@Table(name = "suppliers")
public class Supplier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "contact_name", length = 100)
	private String contactName;

	@Column(name = "phone", length = 30)
	private String phone;

	@Column(name = "email", length = 255)
	private String email;

	@OneToMany(mappedBy = "supplier")
	private List<SupplierAddress> addresses = new ArrayList<>();

	@OneToMany(mappedBy = "supplier")
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<SupplierAddress> getAddresses() {
		return addresses;
	}

	public List<Product> getProducts() {
		return products;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(id=" + id + ", name=" + name + ")";
	}

}

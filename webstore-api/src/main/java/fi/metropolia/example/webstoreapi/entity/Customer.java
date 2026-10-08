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
 * Customer, mapped to {@code customers} (~100k rows of generated data).
 *
 * <p>Collections (addresses, orders) are LAZY: the customer read endpoints
 * fetch them explicitly (JOIN FETCH / explicit collection access), while most
 * customer queries touch only the row itself. Loading 1:M collections eagerly
 * behind 100k customers would be very expensive.</p>
 */
@Entity
@Table(name = "customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email", nullable = false, length = 255)
	private String email;

	@Column(name = "phone", length = 30)
	private String phone;

	@OneToMany(mappedBy = "customer")
	private List<CustomerAddress> addresses = new ArrayList<>();

	@OneToMany(mappedBy = "customer")
	private List<Order> orders = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<CustomerAddress> getAddresses() {
		return addresses;
	}

	public List<Order> getOrders() {
		return orders;
	}

}

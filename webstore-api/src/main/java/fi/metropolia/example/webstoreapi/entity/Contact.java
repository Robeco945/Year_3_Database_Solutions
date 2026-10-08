package fi.metropolia.example.webstoreapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contact, mapped to {@code contacts} (~500k rows; generated email + opaque
 * 32-char reference used as the contact record's identity).
 *
 * <p>Kept as a standalone resource: the provided schema creates no FK between
 * customers and contacts, so the API creates/updates the customer's contact
 * record through its own endpoint (plan §2) rather than a navigable
 * {@code @OneToMany}. No silent schema change invented here.</p>
 */
@Entity
@Table(name = "contacts")
public class Contact {

	public static final int REFERENCE_LENGTH = 32;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Integer id;

	@Column(name = "email", nullable = false, length = 255)
	private String email;

	@Column(name = "reference", nullable = false, updatable = false, length = REFERENCE_LENGTH)
	private String reference;

	public Integer getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

}

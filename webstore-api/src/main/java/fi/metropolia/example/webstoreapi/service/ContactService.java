package fi.metropolia.example.webstoreapi.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.ContactDto;
import fi.metropolia.example.webstoreapi.dto.ContactUpdateRequest;
import fi.metropolia.example.webstoreapi.entity.Contact;
import fi.metropolia.example.webstoreapi.entity.Customer;
import fi.metropolia.example.webstoreapi.exception.ConflictException;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.ContactRepository;
import fi.metropolia.example.webstoreapi.repository.CustomerRepository;

/**
 * Customer contact CRUD (plan §2, §4 #20). The provided schema has no FK
 * between {@code customers} and {@code contacts}, so a customer's contact
 * record is matched by the customer's email address (documented in the README).
 */
@Service
public class ContactService {

	private final ContactRepository contactRepository;
	private final CustomerRepository customerRepository;

	public ContactService(ContactRepository contactRepository, CustomerRepository customerRepository) {
		this.contactRepository = contactRepository;
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public ContactDto getForCustomer(Integer customerId) {
		Customer customer = findCustomer(customerId);
		return contactRepository.findFirstByEmailIgnoreCaseOrderByIdAsc(customer.getEmail()).map(this::toDto)
				.orElseThrow(() -> new ResourceNotFoundException(
						"No contact record for customer " + customerId + " (email " + customer.getEmail() + ")"));
	}

	@Transactional
	public ContactDto createForCustomer(Integer customerId) {
		Customer customer = findCustomer(customerId);
		if (contactRepository.findFirstByEmailIgnoreCaseOrderByIdAsc(customer.getEmail()).isPresent()) {
			throw new ConflictException("Customer " + customerId + " already has a contact record");
		}
		Contact contact = new Contact();
		contact.setEmail(customer.getEmail());
		contact.setReference(UUID.randomUUID().toString().replace("-", ""));
		return toDto(contactRepository.save(contact));
	}

	@Transactional
	public ContactDto updateContact(Integer id, ContactUpdateRequest request) {
		Contact contact = contactRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Contact not found: id=" + id));
		if (request == null || request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("email is required");
		}
		contact.setEmail(request.email().trim());
		return toDto(contactRepository.save(contact));
	}

	private Customer findCustomer(Integer id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
	}

	private ContactDto toDto(Contact contact) {
		return new ContactDto(contact.getId(), contact.getEmail(), contact.getReference());
	}

}

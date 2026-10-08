package fi.metropolia.example.webstoreapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.ContactDto;
import fi.metropolia.example.webstoreapi.dto.ContactUpdateRequest;
import fi.metropolia.example.webstoreapi.service.ContactService;

/**
 * Customer contact CRUD (plan §4 #20). The contact record is linked to the
 * customer by matching email (the provided schema has no FK).
 */
@RestController
public class ContactController {

	private final ContactService contactService;

	public ContactController(ContactService contactService) {
		this.contactService = contactService;
	}

	@GetMapping("/customers/{customerId}/contact")
	public ContactDto getContact(@PathVariable("customerId") Integer customerId) {
		return contactService.getForCustomer(customerId);
	}

	@PostMapping("/customers/{customerId}/contact")
	public ResponseEntity<ContactDto> createContact(@PathVariable("customerId") Integer customerId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(contactService.createForCustomer(customerId));
	}

	@PutMapping("/contact/{id}")
	public ContactDto updateContact(@PathVariable("id") Integer id, @RequestBody ContactUpdateRequest request) {
		return contactService.updateContact(id, request);
	}

}

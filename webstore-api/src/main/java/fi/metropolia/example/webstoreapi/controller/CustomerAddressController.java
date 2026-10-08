package fi.metropolia.example.webstoreapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.CustomerAddressDto;
import fi.metropolia.example.webstoreapi.dto.CustomerAddressWriteDto;
import fi.metropolia.example.webstoreapi.service.CustomerAddressService;

/**
 * Customer address CRUD (plan §4 #19): customers manage their own delivery
 * addresses; an address referenced by an order cannot be deleted.
 */
@RestController
public class CustomerAddressController {

	private final CustomerAddressService addressService;

	public CustomerAddressController(CustomerAddressService addressService) {
		this.addressService = addressService;
	}

	@GetMapping("/customers/{customerId}/addresses")
	public List<CustomerAddressDto> listAddresses(@PathVariable("customerId") Integer customerId) {
		return addressService.listForCustomer(customerId);
	}

	@PostMapping("/customers/{customerId}/addresses")
	public ResponseEntity<CustomerAddressDto> createAddress(@PathVariable("customerId") Integer customerId,
			@RequestBody CustomerAddressWriteDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(customerId, dto));
	}

	@GetMapping("/customeraddresses/{id}")
	public CustomerAddressDto getAddress(@PathVariable("id") Integer id) {
		return addressService.getAddress(id);
	}

	@PutMapping("/customeraddresses/{id}")
	public CustomerAddressDto updateAddress(@PathVariable("id") Integer id,
			@RequestBody CustomerAddressWriteDto dto) {
		return addressService.updateAddress(id, dto);
	}

	@DeleteMapping("/customeraddresses/{id}")
	public ResponseEntity<Void> deleteAddress(@PathVariable("id") Integer id) {
		addressService.deleteAddress(id);
		return ResponseEntity.noContent().build();
	}

}

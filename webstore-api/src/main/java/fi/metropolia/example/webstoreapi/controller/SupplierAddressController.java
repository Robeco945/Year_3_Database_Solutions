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

import fi.metropolia.example.webstoreapi.dto.SupplierAddressDto;
import fi.metropolia.example.webstoreapi.dto.SupplierAddressWriteDto;
import fi.metropolia.example.webstoreapi.service.SupplierService;

/**
 * Supplier address management (plan §2, admin view): the 1:M addresses of a
 * supplier.
 */
@RestController
public class SupplierAddressController {

	private final SupplierService supplierService;

	public SupplierAddressController(SupplierService supplierService) {
		this.supplierService = supplierService;
	}

	@GetMapping("/suppliers/{supplierId}/addresses")
	public List<SupplierAddressDto> listAddresses(@PathVariable("supplierId") Integer supplierId) {
		return supplierService.listAddresses(supplierId);
	}

	@PostMapping("/suppliers/{supplierId}/addresses")
	public ResponseEntity<SupplierAddressDto> addAddress(@PathVariable("supplierId") Integer supplierId,
			@RequestBody SupplierAddressWriteDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.addAddress(supplierId, dto));
	}

	@PutMapping("/supplieraddresses/{id}")
	public SupplierAddressDto updateAddress(@PathVariable("id") Integer id,
			@RequestBody SupplierAddressWriteDto dto) {
		return supplierService.updateAddress(id, dto);
	}

	@DeleteMapping("/supplieraddresses/{id}")
	public ResponseEntity<Void> deleteAddress(@PathVariable("id") Integer id) {
		supplierService.deleteAddress(id);
		return ResponseEntity.noContent().build();
	}

}

package fi.metropolia.example.webstoreapi.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.SupplierDetailDto;
import fi.metropolia.example.webstoreapi.dto.SupplierDto;
import fi.metropolia.example.webstoreapi.dto.SupplierWriteDto;
import fi.metropolia.example.webstoreapi.service.SupplierService;

/**
 * Supplier admin CRUD (plan §2): full C-R-U-D incl. the 1:M addresses
 * (address endpoints are in {@link SupplierAddressController}).
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

	private final SupplierService supplierService;

	public SupplierController(SupplierService supplierService) {
		this.supplierService = supplierService;
	}

	@GetMapping
	public PageResponseDto<SupplierDto> listSuppliers(@RequestParam(name = "search", required = false) String search,
			@PageableDefault(size = 20, sort = "name") Pageable pageable) {
		return supplierService.listSuppliers(search, pageable);
	}

	@GetMapping("/{id}")
	public SupplierDetailDto getSupplier(@PathVariable("id") Integer id) {
		return supplierService.getSupplier(id);
	}

	@PostMapping
	public ResponseEntity<SupplierDetailDto> createSupplier(@RequestBody SupplierWriteDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(dto));
	}

	@PutMapping("/{id}")
	public SupplierDetailDto updateSupplier(@PathVariable("id") Integer id, @RequestBody SupplierWriteDto dto) {
		return supplierService.updateSupplier(id, dto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteSupplier(@PathVariable("id") Integer id) {
		supplierService.deleteSupplier(id);
		return ResponseEntity.noContent().build();
	}

}

package fi.metropolia.example.webstoreapi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.SupplierAddressDto;
import fi.metropolia.example.webstoreapi.dto.SupplierAddressWriteDto;
import fi.metropolia.example.webstoreapi.dto.SupplierDetailDto;
import fi.metropolia.example.webstoreapi.dto.SupplierDto;
import fi.metropolia.example.webstoreapi.dto.SupplierWriteDto;
import fi.metropolia.example.webstoreapi.entity.Supplier;
import fi.metropolia.example.webstoreapi.entity.SupplierAddress;
import fi.metropolia.example.webstoreapi.exception.ConflictException;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.ProductRepository;
import fi.metropolia.example.webstoreapi.repository.SupplierAddressRepository;
import fi.metropolia.example.webstoreapi.repository.SupplierRepository;

/**
 * Supplier admin CRUD incl. its 1:M addresses (plan §2). A supplier that still
 * has products cannot be deleted (FK products.supplier_id).
 */
@Service
public class SupplierService {

	private final SupplierRepository supplierRepository;
	private final SupplierAddressRepository addressRepository;
	private final ProductRepository productRepository;

	public SupplierService(SupplierRepository supplierRepository, SupplierAddressRepository addressRepository,
			ProductRepository productRepository) {
		this.supplierRepository = supplierRepository;
		this.addressRepository = addressRepository;
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public PageResponseDto<SupplierDto> listSuppliers(String search, Pageable pageable) {
		Page<Supplier> page = search == null || search.isBlank()
				? supplierRepository.findAll(pageable)
				: supplierRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
		return PageResponseDto.of(page.map(this::toDto));
	}

	@Transactional(readOnly = true)
	public SupplierDetailDto getSupplier(Integer id) {
		return toDetail(findSupplier(id));
	}

	@Transactional
	public SupplierDetailDto createSupplier(SupplierWriteDto dto) {
		validate(dto);
		Supplier supplier = new Supplier();
		apply(supplier, dto);
		return toDetail(supplierRepository.save(supplier));
	}

	@Transactional
	public SupplierDetailDto updateSupplier(Integer id, SupplierWriteDto dto) {
		Supplier supplier = findSupplier(id);
		validate(dto);
		apply(supplier, dto);
		return toDetail(supplierRepository.save(supplier));
	}

	@Transactional
	public void deleteSupplier(Integer id) {
		Supplier supplier = findSupplier(id);
		if (productRepository.countBySupplierId(id) > 0) {
			throw new ConflictException("Supplier " + id + " still has products and cannot be deleted (FK suppliers)");
		}
		addressRepository.deleteAll(addressRepository.findBySupplierId(id));
		supplierRepository.delete(supplier);
	}

	@Transactional(readOnly = true)
	public List<SupplierAddressDto> listAddresses(Integer supplierId) {
		findSupplier(supplierId);
		return addressRepository.findBySupplierId(supplierId).stream().map(this::toAddressDto).toList();
	}

	@Transactional
	public SupplierAddressDto addAddress(Integer supplierId, SupplierAddressWriteDto dto) {
		Supplier supplier = findSupplier(supplierId);
		validateAddress(dto);
		SupplierAddress address = new SupplierAddress();
		address.setSupplier(supplier);
		applyAddress(address, dto);
		return toAddressDto(addressRepository.save(address));
	}

	@Transactional
	public SupplierAddressDto updateAddress(Integer addressId, SupplierAddressWriteDto dto) {
		SupplierAddress address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Supplier address not found: id=" + addressId));
		validateAddress(dto);
		applyAddress(address, dto);
		return toAddressDto(addressRepository.save(address));
	}

	@Transactional
	public void deleteAddress(Integer addressId) {
		SupplierAddress address = addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Supplier address not found: id=" + addressId));
		addressRepository.delete(address);
	}

	private Supplier findSupplier(Integer id) {
		return supplierRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Supplier not found: id=" + id));
	}

	private void validate(SupplierWriteDto dto) {
		if (dto == null || dto.name() == null || dto.name().isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
	}

	private void validateAddress(SupplierAddressWriteDto dto) {
		if (dto == null || dto.streetAddress() == null || dto.streetAddress().isBlank()) {
			throw new IllegalArgumentException("streetAddress is required");
		}
		if (dto.city() == null || dto.city().isBlank()) {
			throw new IllegalArgumentException("city is required");
		}
	}

	private void apply(Supplier supplier, SupplierWriteDto dto) {
		supplier.setName(dto.name().trim());
		supplier.setContactName(dto.contactName());
		supplier.setPhone(dto.phone());
		supplier.setEmail(dto.email());
	}

	private void applyAddress(SupplierAddress address, SupplierAddressWriteDto dto) {
		address.setStreetAddress(dto.streetAddress().trim());
		address.setPostalCode(dto.postalCode());
		address.setCity(dto.city().trim());
		address.setCountry(dto.country());
	}

	private SupplierDto toDto(Supplier supplier) {
		return new SupplierDto(supplier.getId(), supplier.getName(), supplier.getContactName(), supplier.getPhone(),
				supplier.getEmail());
	}

	private SupplierDetailDto toDetail(Supplier supplier) {
		List<SupplierAddressDto> addresses = addressRepository.findBySupplierId(supplier.getId()).stream()
				.map(this::toAddressDto).toList();
		return new SupplierDetailDto(supplier.getId(), supplier.getName(), supplier.getContactName(),
				supplier.getPhone(), supplier.getEmail(), addresses);
	}

	private SupplierAddressDto toAddressDto(SupplierAddress address) {
		return new SupplierAddressDto(address.getId(), address.getSupplier().getId(), address.getStreetAddress(),
				address.getPostalCode(), address.getCity(), address.getCountry());
	}

}

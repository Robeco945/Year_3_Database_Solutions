package fi.metropolia.example.webstoreapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.metropolia.example.webstoreapi.dto.CustomerAddressDto;
import fi.metropolia.example.webstoreapi.dto.CustomerAddressWriteDto;
import fi.metropolia.example.webstoreapi.entity.Customer;
import fi.metropolia.example.webstoreapi.entity.CustomerAddress;
import fi.metropolia.example.webstoreapi.exception.ConflictException;
import fi.metropolia.example.webstoreapi.exception.ResourceNotFoundException;
import fi.metropolia.example.webstoreapi.repository.CustomerAddressRepository;
import fi.metropolia.example.webstoreapi.repository.CustomerRepository;
import fi.metropolia.example.webstoreapi.repository.OrderRepository;

/**
 * Customer delivery address CRUD (plan §2, §4 #19). An address referenced as
 * an order's {@code shipping_address_id} cannot be deleted (FK constraint);
 * that conflict is reported as 409 before hitting the database.
 */
@Service
public class CustomerAddressService {

	private final CustomerAddressRepository addressRepository;
	private final CustomerRepository customerRepository;
	private final OrderRepository orderRepository;

	public CustomerAddressService(CustomerAddressRepository addressRepository, CustomerRepository customerRepository,
			OrderRepository orderRepository) {
		this.addressRepository = addressRepository;
		this.customerRepository = customerRepository;
		this.orderRepository = orderRepository;
	}

	@Transactional(readOnly = true)
	public List<CustomerAddressDto> listForCustomer(Integer customerId) {
		findCustomer(customerId);
		return addressRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public CustomerAddressDto getAddress(Integer id) {
		return toDto(findAddress(id));
	}

	@Transactional
	public CustomerAddressDto createAddress(Integer customerId, CustomerAddressWriteDto dto) {
		Customer customer = findCustomer(customerId);
		validate(dto);
		CustomerAddress address = new CustomerAddress();
		address.setCustomer(customer);
		apply(address, dto);
		return toDto(addressRepository.save(address));
	}

	@Transactional
	public CustomerAddressDto updateAddress(Integer id, CustomerAddressWriteDto dto) {
		CustomerAddress address = findAddress(id);
		validate(dto);
		apply(address, dto);
		return toDto(addressRepository.save(address));
	}

	@Transactional
	public void deleteAddress(Integer id) {
		CustomerAddress address = findAddress(id);
		if (orderRepository.existsByShippingAddressId(id)) {
			throw new ConflictException(
					"Address " + id + " is used as a shipping address by orders and cannot be deleted");
		}
		addressRepository.delete(address);
	}

	private Customer findCustomer(Integer id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
	}

	private CustomerAddress findAddress(Integer id) {
		return addressRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Address not found: id=" + id));
	}

	private void validate(CustomerAddressWriteDto dto) {
		if (dto == null || dto.streetAddress() == null || dto.streetAddress().isBlank()) {
			throw new IllegalArgumentException("streetAddress is required");
		}
		if (dto.city() == null || dto.city().isBlank()) {
			throw new IllegalArgumentException("city is required");
		}
	}

	private void apply(CustomerAddress address, CustomerAddressWriteDto dto) {
		address.setStreetAddress(dto.streetAddress().trim());
		address.setPostalCode(dto.postalCode());
		address.setCity(dto.city().trim());
		address.setCountry(dto.country());
	}

	private CustomerAddressDto toDto(CustomerAddress address) {
		return new CustomerAddressDto(address.getId(), address.getCustomer().getId(), address.getStreetAddress(),
				address.getPostalCode(), address.getCity(), address.getCountry());
	}

}

package fi.metropolia.example.webstoreapi.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.CustomerDetailDto;
import fi.metropolia.example.webstoreapi.dto.CustomerDto;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.dto.TopSpenderDto;
import fi.metropolia.example.webstoreapi.service.CustomerService;

/**
 * Customer read endpoints for the webshop view (plan §2: customer data is
 * read-only through the shop API; plan §4 #6–#8).
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public PageResponseDto<CustomerDto> listCustomers(@RequestParam(name = "search", required = false) String search,
			@PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
		return customerService.listCustomers(search, pageable);
	}

	@GetMapping("/{id}")
	public CustomerDetailDto getCustomer(@PathVariable("id") Integer id) {
		return customerService.getCustomer(id);
	}

	@GetMapping("/without-orders")
	public PageResponseDto<CustomerDto> customersWithoutOrders(
			@PageableDefault(size = 20, sort = "id") Pageable pageable) {
		return customerService.customersWithoutOrders(pageable);
	}

	@GetMapping("/top-spenders")
	public List<TopSpenderDto> topSpenders(@RequestParam(name = "limit", defaultValue = "10") int limit) {
		return customerService.topSpenders(limit);
	}

}

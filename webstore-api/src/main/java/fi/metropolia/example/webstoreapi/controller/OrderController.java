package fi.metropolia.example.webstoreapi.controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.metropolia.example.webstoreapi.dto.CartEditRequest;
import fi.metropolia.example.webstoreapi.dto.OrderCreateRequest;
import fi.metropolia.example.webstoreapi.dto.OrderDetailDto;
import fi.metropolia.example.webstoreapi.dto.OrderStatusUpdateRequest;
import fi.metropolia.example.webstoreapi.dto.OrderSummaryDto;
import fi.metropolia.example.webstoreapi.dto.PageResponseDto;
import fi.metropolia.example.webstoreapi.entity.OrderStatus;
import fi.metropolia.example.webstoreapi.service.OrderService;

/**
 * Order management endpoints (plan §4 #9–#15, #21): order aggregate CRUD with
 * checkout, transactional cart editing and the status lifecycle.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public PageResponseDto<OrderSummaryDto> listOrders(
			@RequestParam(name = "status", required = false) OrderStatus status,
			@RequestParam(name = "customerId", required = false) Integer customerId,
			@PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return orderService.searchOrders(status, customerId, null, null, pageable);
	}

	@GetMapping("/search")
	public PageResponseDto<OrderSummaryDto> searchOrders(
			@RequestParam(name = "status", required = false) OrderStatus status,
			@RequestParam(name = "customerId", required = false) Integer customerId,
			@RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
			@RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
			@PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return orderService.searchOrders(status, customerId, dateFrom, dateTo, pageable);
	}

	@GetMapping("/customer/{customerId}")
	public PageResponseDto<OrderSummaryDto> getCustomerOrders(
			@PathVariable("customerId") Integer customerId,
			@RequestParam(name = "status", required = false) OrderStatus status,
			@PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return orderService.searchOrders(status, customerId, null, null, pageable);
	}

	@GetMapping("/{id}")
	public OrderDetailDto getOrder(@PathVariable("id") Integer id) {
		return orderService.getOrder(id);
	}

	@PostMapping
	public ResponseEntity<OrderDetailDto> checkout(@RequestBody OrderCreateRequest request) {
		OrderDetailDto created = orderService.checkout(request);
		return ResponseEntity.created(URI.create("/orders/" + created.id())).body(created);
	}

	@PutMapping("/{id}/status")
	public OrderDetailDto changeStatus(@PathVariable("id") Integer id,
			@RequestBody OrderStatusUpdateRequest request) {
		return orderService.changeStatus(id, request);
	}

	@PatchMapping("/{id}/items")
	public OrderDetailDto editCart(@PathVariable("id") Integer id, @RequestBody CartEditRequest request) {
		return orderService.editCart(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable("id") Integer id) {
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}

}

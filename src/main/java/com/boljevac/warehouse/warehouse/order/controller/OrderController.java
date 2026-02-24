package com.boljevac.warehouse.warehouse.order.controller;

import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/products")
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		return ResponseEntity.ok(orderService.getProducts());
	}


	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody@Valid OrderRequest orderRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequest));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<OrderResponse> cancelOrderById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));
	}
}

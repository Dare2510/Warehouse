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
@RequestMapping("/api/warehouse/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	//Overview of available goods - Inventory

	@GetMapping("/products")
	public ResponseEntity<List<ProductResponse>> getAllProducts() {
		return ResponseEntity.ok(orderService.getProducts());
	}

	//create Order

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@RequestBody@Valid OrderRequest orderRequest) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderRequest));
	}
	//Cancel an Order if the requirements are met (Status = Order_placed)

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<OrderResponse> cancelOrderById(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));
	}
}

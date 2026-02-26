package com.boljevac.warehouse.warehouse.processor.controller;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorResponse;
import com.boljevac.warehouse.warehouse.processor.service.ProcessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/warehouse/processing")
public class ProcessorController {

	private final ProcessorService processorService;

	public ProcessorController(ProcessorService processorService) {
		this.processorService = processorService;
	}
	//get Orders by status

	@PostMapping
	public ResponseEntity<List<ProcessorResponse>> getOrders(@RequestBody@Valid ProcessorRequest processorRequest) {

		return ResponseEntity.status(HttpStatus.OK).body(processorService.getOrders(processorRequest));
	}
	//change the status of an Order

	@PutMapping("/statusChange/{id}/{status}")
	public ResponseEntity<ProcessorResponse> changeStatusToProcessing(@PathVariable Long id,@PathVariable OrderStatus status) {
		return ResponseEntity.status(HttpStatus.OK).body(processorService.changeOrderStatus(id,status));
	}
	//delete canceled Order by ID - Orderstatus must be canceled

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
		processorService.deleteOrderById(id);
		return ResponseEntity.noContent().build();
	}
	//Move all Orders with status "shipped" to "shippedOrdersRepo"

	@GetMapping("/archive")
	public ResponseEntity<Void> moveShippedOrders() {
		processorService.moveShippedOrders();
		return ResponseEntity.ok().build();
	}
	//delete all canceled Orders

	@DeleteMapping("/deleteCancelled")
	public ResponseEntity<Void> deleteCancelledOrders() {
		processorService.deleteCancelledOrders();
		return ResponseEntity.noContent().build();
	}


}

package com.boljevac.warehouse.warehouse.processor.controller;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
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

	@PostMapping
	public ResponseEntity<List<ProcessorResponse>> getOrders(@RequestBody @Valid ProcessorRequest processorRequest) {

		return ResponseEntity.status(HttpStatus.OK).body(processorService.getOrders(processorRequest));
	}

	//Change status, sequence must be followed : ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED
	@PutMapping("/statusChange/{id}/{orderStatuses}")
	public ResponseEntity<ProcessorResponse> changeStatusToProcessing(@PathVariable Long id, @PathVariable OrderStatuses orderStatuses) {
		return ResponseEntity.status(HttpStatus.OK).body(processorService.changeOrderStatus(id, orderStatuses));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
		processorService.deleteOrderById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/archive")
	public ResponseEntity<Void> moveShippedOrders() {
		processorService.moveShippedOrders();
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/deleteCancelled")
	public ResponseEntity<Void> deleteCancelledOrders() {
		processorService.deleteCancelledOrders();
		return ResponseEntity.noContent().build();
	}


}

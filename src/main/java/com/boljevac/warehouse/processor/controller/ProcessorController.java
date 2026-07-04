package com.boljevac.warehouse.processor.controller;

import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.processor.dto.ProcessorResponse;
import com.boljevac.warehouse.processor.service.ProcessorService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/warehouse/processing")
@PreAuthorize("hasAnyRole('ADMIN','CLERK')")
public class ProcessorController {

	private final ProcessorService processorService;

	public ProcessorController(ProcessorService processorService) {
		this.processorService = processorService;
	}

	@PostMapping
	public ResponseEntity<List<ProcessorResponse>> getOrders(@RequestBody @Valid ProcessorRequest processorRequest) {

		return ResponseEntity.status(HttpStatus.OK).body(processorService.getListOfOrdersByStatus(processorRequest));
	}

	//Change status, sequence must be followed : ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED
	@PutMapping("/statusChange/{id}/{orderStatus}")
	public ResponseEntity<ProcessorResponse> changeStatusToProcessing(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable Long id, @PathVariable OrderStatus orderStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(processorService.changeStatusOfOrder(authenticatedUser,id, orderStatus));
	}

	//Only canceled orders
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
		processorService.deleteOrderById(id);
		return ResponseEntity.noContent().build();
	}

	//Only shipped orders
	@PatchMapping("/archive")
	public ResponseEntity<Void> moveShippedOrders(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
		processorService.archiveShippedOrders(authenticatedUser);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/deleteCancelled")
	public ResponseEntity<Void> deleteCancelledOrders() {
		processorService.deleteAllCancelledOrders();
		return ResponseEntity.noContent().build();
	}


}

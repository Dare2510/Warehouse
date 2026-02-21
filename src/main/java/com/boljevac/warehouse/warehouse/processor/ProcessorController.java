package com.boljevac.warehouse.warehouse.processor;

import com.boljevac.warehouse.warehouse.order.OrderStatus;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/processor")
public class ProcessorController {

	private final ProcessorService processorService;

	public ProcessorController(ProcessorService processorService) {
		this.processorService = processorService;
	}

	@PostMapping
	public ResponseEntity<List<ProcessorResponse>> getOpenOrders(@RequestBody@Valid ProcessorRequest processorRequest) {

		return ResponseEntity.status(HttpStatus.OK).body(processorService.getOrders(processorRequest));
	}

	@PutMapping("/changeStatus/{id}/{status}")
	public ResponseEntity<ProcessorResponse> changeStatusToProcessing(@PathVariable Long id,@PathVariable OrderStatus status) {
		return ResponseEntity.status(HttpStatus.OK).body(processorService.changeOrderStatus(id,status));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteOrderById(@PathVariable Long id) {
		processorService.deleteOrderById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/move")
	public ResponseEntity<Void> moveShippedOrders() {
		processorService.moveShippedOrders();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/deleteCancelled")
	public ResponseEntity<Void> deleteCancelledOrders() {
		processorService.deleteCancelledOrders();
		return ResponseEntity.noContent().build();
	}


}

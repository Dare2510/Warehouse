package com.boljevac.warehouse.warehouse.inventory.controller;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inventory")
public class InventoryController {

	private final InventoryService inventoryService;


	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}
	@GetMapping("/{id}")
	public  ResponseEntity<InventoryResponse> getStock(@PathVariable Long id) {
		return ResponseEntity.ok(inventoryService.getInventoryResponse(id));
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> addStock(@RequestBody InventoryRequest inventoryRequest) {
		return ResponseEntity.ok(inventoryService.createStock(inventoryRequest));
	}


}

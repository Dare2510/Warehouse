package com.boljevac.warehouse.warehouse.inventory.controller;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse/inventory")
public class InventoryController {

	private final InventoryService inventoryService;


	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping("/store")
	public ResponseEntity<InventoryResponse> storeInventory(@RequestBody InventoryRequest inventoryRequest) {

		return ResponseEntity.status(HttpStatus.OK).body(inventoryService.storeProduct(inventoryRequest));
	}

	@GetMapping("/getAll")
	public ResponseEntity<List<InventoryResponse>> getAllInventory() {
		return ResponseEntity.status(HttpStatus.OK).body(inventoryService.getInventory());
	}


}

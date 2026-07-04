package com.boljevac.warehouse.inventory.controller;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.inventory.service.InventoryService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/inventory")
@PreAuthorize("hasAnyRole('ADMIN','CLERK')")
public class InventoryController {

	private final InventoryService inventoryService;


	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<InventoryResponse> getStock(@PathVariable Long id) {
		return ResponseEntity.ok(inventoryService.getInventoryResponse(id));
	}

	@PostMapping
	public ResponseEntity<InventoryResponse> addStock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,@RequestBody @Valid InventoryRequest inventoryRequest) {
		return ResponseEntity.ok(inventoryService.createStock(authenticatedUser,inventoryRequest));
	}


}

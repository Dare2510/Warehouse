package com.boljevac.warehouse.warehouse.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LocationsRequest {
	@NotNull(message = "inventoryId is required")
	@Positive(message = "inventoryId must be > 0")
	private Long inventoryId;

	@NotNull(message = "quantity is required")
	@Positive(message = "quantity must be > 0")
	private Integer quantity;

	public LocationsRequest(Long inventoryId, int quantity) {
		this.inventoryId = inventoryId;
		this.quantity = quantity;

	}

	public Long getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(Long inventoryId) {
		this.inventoryId = inventoryId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

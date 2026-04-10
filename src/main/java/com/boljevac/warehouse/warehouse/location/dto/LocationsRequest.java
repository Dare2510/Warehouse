package com.boljevac.warehouse.warehouse.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}

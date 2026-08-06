package com.boljevac.warehouse.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LocationsRequest {
	@NotNull(message = "inventoryId is required")
	@Positive(message = "inventoryId must be > 0")
	private Long inventoryId;

	@NotNull(message = "quantity is required")
	@Positive(message = "quantity must be > 0")
	private Integer quantity;
}

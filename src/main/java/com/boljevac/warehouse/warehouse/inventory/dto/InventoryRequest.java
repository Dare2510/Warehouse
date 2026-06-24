package com.boljevac.warehouse.warehouse.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryRequest {

	@NotNull(message = "Product ID is required")
	@Positive(message = "Product ID must be positive")
	private Long productId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be > 0")
	private Integer quantity;

	public InventoryRequest(Long productId, int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}
}

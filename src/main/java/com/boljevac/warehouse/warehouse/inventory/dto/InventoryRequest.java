package com.boljevac.warehouse.warehouse.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InventoryRequest {

	@NotNull(message = "Product ID is required")
	@Positive(message = "Product ID must be positive")
	private Long id;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be > 0")
	private int quantity;

	public InventoryRequest(Long productId,int quantity) {
		this.id = productId;
		this.quantity = quantity;
	}

	public Long getProductId() {
		return id;
	}

	public void setProductId(Long productId) {
		this.id = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

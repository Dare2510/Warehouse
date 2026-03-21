package com.boljevac.warehouse.warehouse.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InventoryRequest {

	@NotNull(message = "Product ID is required")
	@Positive(message = "Product ID must be positive")
	private Long productId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be > 0")
	private Integer quantity;

	public InventoryRequest(Long productId,int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

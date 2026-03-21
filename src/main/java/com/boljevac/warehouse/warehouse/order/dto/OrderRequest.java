package com.boljevac.warehouse.warehouse.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequest {


	@Positive(message = "ProductId must be positive")
	@NotNull(message = "ProductId must not be null")
	private Long productId;

	@Positive(message = "quantity must be positive")
	@NotNull(message = "quantity must not be null")
	private int quantity;

	public OrderRequest(Long productId, int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public Long getProductId() {
		return productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

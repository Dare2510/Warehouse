package com.boljevac.warehouse.warehouse.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderRequest {

	@Positive(message = "id must be positive")
	@NotNull(message = "id must not be null")
	private Long id;

	@Positive(message = "quantity must be positive")
	@NotNull(message = "quantity must not be null")
	private int quantity;

	public OrderRequest(Long id, int quantity) {
		this.id = id;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}
	public int getQuantity() {
		return quantity;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

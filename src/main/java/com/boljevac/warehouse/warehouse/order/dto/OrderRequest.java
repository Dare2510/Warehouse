package com.boljevac.warehouse.warehouse.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {


	@Positive(message = "ProductId must be positive")
	@NotNull(message = "ProductId must not be null")
	private Long productId;

	@Positive(message = "quantity must be positive")
	@NotNull(message = "quantity must not be null")
	private Integer quantity;

	public OrderRequest(Long productId, int quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}
}

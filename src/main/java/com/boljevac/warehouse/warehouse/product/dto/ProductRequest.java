package com.boljevac.warehouse.warehouse.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRequest {

	@NotBlank
	@Pattern(regexp = "^[A-Za-z]{4,30}$", message = "Not a valid Product name, only Characters allowed")
	private String product;


	@Positive(message = "Value must be over 0")
	private BigDecimal value;


	@PositiveOrZero(message = "Stock cannot be negative")
	private int quantity;


	public ProductRequest(String product, BigDecimal value, int quantity) {
		this.product = product;
		this.value = value;
		this.quantity = quantity;
	}

	public String getProduct() {
		return product;
	}

	public BigDecimal getValue() {
		return value;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

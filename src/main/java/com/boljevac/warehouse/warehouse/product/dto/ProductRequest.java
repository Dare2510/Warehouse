package com.boljevac.warehouse.warehouse.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRequest {

	@NotBlank(message = "product is required")
	@Pattern(regexp = "^[A-Za-z]{4,30}$", message = "Not a valid Product name, only Characters allowed")
	private String product;

	@NotNull(message = "value is required")
	@Positive(message = "Value must be over 0")
	private BigDecimal value;

	@NotNull(message = "weight is required")
	@Positive(message = "weight must be >0")
	private double weight;

//	@NotNull(message = "quantity is required")
//	@PositiveOrZero(message = "Stock cannot be negative")
//	private int quantity;


	public ProductRequest(String product, BigDecimal value, double weight) {
		this.product = product;
		this.value = value;
		this.weight = weight;
	//	this.quantity = quantity;
	}

	public String getProduct() {
		return product;
	}

	public BigDecimal getValue() {
		return value;
	}

//	public int getQuantity() {
//		return quantity;
//	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

//	public void setQuantity(int quantity) {
//		this.quantity = quantity;
//	}
}

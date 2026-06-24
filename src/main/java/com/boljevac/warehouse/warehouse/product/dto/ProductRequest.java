package com.boljevac.warehouse.warehouse.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

	@NotBlank(message = "product is required")
	@Pattern(regexp = "^[A-Za-z]{4,30}$", message = "Product name -> only Characters allowed, " +
			"minimum length=4 and maximum length=30")
	private String product;

	@NotNull(message = "value is required")
	@Positive(message = "Value must be over 0")
	private BigDecimal value;

	@NotNull(message = "weight is required")
	@Positive(message = "weight must be >0")
	private Double weight;

	public ProductRequest(String product, BigDecimal value, double weight) {
		this.product = product;
		this.value = value;
		this.weight = weight;
	}
}

package com.boljevac.warehouse.warehouse.product.dto;

import java.math.BigDecimal;

public class ProductResponse {

	private final Long id;
	private final String name;
	private final BigDecimal value;
	private final int quantity;
	private final BigDecimal totalValue;


	public ProductResponse(Long id, String name, BigDecimal value, int quantity) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.quantity = quantity;
		this.totalValue = new BigDecimal(value.doubleValue() * quantity);

	}

	public String getName() {
		return name;
	}

	public BigDecimal getValue() {
		return value;
	}

	public int getQuantity() {
		return quantity;
	}

	public Long getId() {
		return id;
	}
	public BigDecimal getTotalValue() {
		return totalValue;
	}
}

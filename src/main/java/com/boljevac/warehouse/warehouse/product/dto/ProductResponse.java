package com.boljevac.warehouse.warehouse.product.dto;

import java.math.BigDecimal;

public class ProductResponse {

	private final Long id;
	private final String name;
	private final BigDecimal price;
	private final double weight;
//	private final int quantity;
//	private final BigDecimal totalValue;


	public ProductResponse(Long id, String name, BigDecimal price, double weight) {
		this.id = id;
		this.name = name;
		this.price = price;
		//this.quantity = quantity;
	//	this.totalValue = new BigDecimal(value.doubleValue() * quantity);
		this.weight = weight;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getPrice() {
		return price;
	}

//	public int getQuantity() {
//		return quantity;
//	}

	public Long getId() {
		return id;
	}

	public double getWeight() {
		return weight;
	}

//	public BigDecimal getTotalValue() {
//		return totalValue;
//	}
}

package com.boljevac.warehouse.warehouse.inventory.dto;

import com.boljevac.warehouse.warehouse.location.Location;

public class InventoryResponse {

	private final String product;

	private final String location;

	private final int quantity;

	public InventoryResponse(String product, String location, int quantity) {
		this.product = product;
		this.location = location;
		this.quantity = quantity;
	}

	public String getProduct() {
		return product;
	}

	public String getLocation() {
		return location;
	}

	public int getQuantity() {
		return quantity;
	}
}

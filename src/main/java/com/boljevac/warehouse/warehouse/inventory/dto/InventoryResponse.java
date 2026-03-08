package com.boljevac.warehouse.warehouse.inventory.dto;

public class InventoryResponse {

	private final String product;
	private final int quantity;

	public InventoryResponse(String product,int quantity) {
		this.product = product;
		this.quantity = quantity;
	}

	public String getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}
}

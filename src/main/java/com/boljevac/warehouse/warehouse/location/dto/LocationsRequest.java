package com.boljevac.warehouse.warehouse.location.dto;

public class LocationsRequest {

	private Long inventoryId;
	private int quantity;



	public LocationsRequest(Long inventoryId, int quantity) {
		this.inventoryId = inventoryId;
		this.quantity = quantity;

	}

	public Long getInventoryId() {
		return inventoryId;
	}

	public void setInventoryId(Long inventoryId) {
		this.inventoryId = inventoryId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

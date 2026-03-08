package com.boljevac.warehouse.warehouse.location.dto;

public class LocationsResponse {

	private final Long inventoryId;
	private final boolean success;

	public LocationsResponse(Long inventoryId, boolean success) {
		this.inventoryId = inventoryId;
		this.success = success;
	}

	public Long getInventoryId() {
		return inventoryId;
	}

	public boolean isSuccess() {
		return success;
	}
}

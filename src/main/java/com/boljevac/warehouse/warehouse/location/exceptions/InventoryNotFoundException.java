package com.boljevac.warehouse.warehouse.location.exceptions;

public class InventoryNotFoundException extends RuntimeException {
	public InventoryNotFoundException(Long inventoryId) {
		super("Inventory with id " + inventoryId + " not found");
	}
}

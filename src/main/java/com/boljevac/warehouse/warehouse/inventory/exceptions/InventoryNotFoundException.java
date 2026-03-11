package com.boljevac.warehouse.warehouse.inventory.exceptions;

public class InventoryNotFoundException extends RuntimeException {
	public InventoryNotFoundException(Long inventoryId) {
		super("Inventory with id " + inventoryId + " not found");
	}
}

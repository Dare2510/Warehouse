package com.boljevac.warehouse.warehouse.inventory.exceptions;

public class NoInventoryForProductFoundException extends RuntimeException {
	public NoInventoryForProductFoundException(Long productId) {
		super("No inventory for product " + productId + "was found");
	}
}

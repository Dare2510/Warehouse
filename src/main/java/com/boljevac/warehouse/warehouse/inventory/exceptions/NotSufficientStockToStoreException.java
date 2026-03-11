package com.boljevac.warehouse.warehouse.inventory.exceptions;

public class NotSufficientStockToStoreException extends RuntimeException {

	public NotSufficientStockToStoreException(int quantity) {
		super("Not enough available Stock to store "+quantity+" pieces");
	}
}

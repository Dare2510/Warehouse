package com.boljevac.warehouse.order.exception;

public class OrderExceedsStockException extends RuntimeException {

	public OrderExceedsStockException() {
		super("Order exceeds stock, -> Order not possible.");
	}
}

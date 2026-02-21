package com.boljevac.warehouse.warehouse.order.exception;

public class OrderExceedsStockException extends RuntimeException{

	public OrderExceedsStockException() {
		super("Order exceeds stock, -> Order not possible.");
	}
}

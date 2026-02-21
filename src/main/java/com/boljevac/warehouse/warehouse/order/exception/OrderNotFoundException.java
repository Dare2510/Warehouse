package com.boljevac.warehouse.warehouse.order.exception;

public class OrderNotFoundException extends RuntimeException{
	public OrderNotFoundException() {
		super("Order/s not found" );
	}
}

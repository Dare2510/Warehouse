package com.boljevac.warehouse.warehouse.order.exception;

public class StatusChangeInvalidOrderException extends RuntimeException{
	public StatusChangeInvalidOrderException() {
		super("Status change order: " +
				"ORDER_PLACED -> (CANCELLED)/PROCESSING -> PACKAGED -> SHIPPED");

	}
}

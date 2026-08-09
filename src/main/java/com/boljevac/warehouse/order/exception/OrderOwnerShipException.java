package com.boljevac.warehouse.order.exception;

public class OrderOwnerShipException extends RuntimeException {
	public OrderOwnerShipException(Long orderId) {

		super("You don't own order with id " + orderId);
	}
}

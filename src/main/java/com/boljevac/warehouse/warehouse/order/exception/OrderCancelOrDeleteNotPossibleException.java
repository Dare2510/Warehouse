package com.boljevac.warehouse.warehouse.order.exception;

public class OrderCancelOrDeleteNotPossibleException extends RuntimeException {

	public OrderCancelOrDeleteNotPossibleException(Long id) {
		super("Can't cancel/delete order with id " + id);
	}
}

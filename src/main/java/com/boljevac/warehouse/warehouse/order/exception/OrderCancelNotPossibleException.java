package com.boljevac.warehouse.warehouse.order.exception;

public class OrderCancelNotPossibleException extends RuntimeException{
	public OrderCancelNotPossibleException(Long id) {
		super("Can't cancel/delete order with id " + id);
	}
}

package com.boljevac.warehouse.warehouse.order.exception;

public class EmptyOrderRepositoryException extends RuntimeException {

	public EmptyOrderRepositoryException() {
		super("Order repository is empty");
	}
}

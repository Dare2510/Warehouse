package com.boljevac.warehouse.warehouse.product.exception;

public class EmptyProductRepositoryException extends RuntimeException {

	public EmptyProductRepositoryException() {
		super("Product Repository is Empty");
	}
}

package com.boljevac.warehouse.product.exception;

public class EmptyProductRepositoryException extends RuntimeException {

	public EmptyProductRepositoryException() {
		super("Product Repository is Empty");
	}
}

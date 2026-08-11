package com.boljevac.warehouse.product.exception;

public class DeletionProductFailed extends RuntimeException {
	public DeletionProductFailed(Long productId) {
		super("Cannot delete product with id " + productId + " order or inventory exist");
	}
}

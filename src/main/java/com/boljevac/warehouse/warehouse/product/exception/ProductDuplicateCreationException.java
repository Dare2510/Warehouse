package com.boljevac.warehouse.warehouse.product.exception;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;

public class ProductDuplicateCreationException extends RuntimeException {
	public ProductDuplicateCreationException(ProductEntity newProductEntity) {

		super("Cannot create product with duplicate Name");
	}
}

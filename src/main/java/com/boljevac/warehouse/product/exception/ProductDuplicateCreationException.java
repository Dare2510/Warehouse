package com.boljevac.warehouse.product.exception;

import com.boljevac.warehouse.product.entity.ProductEntity;

public class ProductDuplicateCreationException extends RuntimeException {

	public ProductDuplicateCreationException(ProductEntity newProductEntity) {
		super("Cannot create product with duplicate Name: " + newProductEntity.getProduct());
	}
}

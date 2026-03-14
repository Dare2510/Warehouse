package com.boljevac.warehouse.warehouse.product.service;

import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

	ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public ProductEntity getProductById(Long id) throws ProductNotFoundException {
		return productRepository.findById(id).
				orElseThrow(() -> new ProductNotFoundException(id));
	}

	public ProductResponse createAndValidateNewProduct(ProductRequest productRequest) {
		ProductEntity newProduct = new ProductEntity(
				productRequest.getProduct(),
				productRequest.getValue(),
				productRequest.getWeight()
		);

		boolean checkForDuplicate = productRepository.existsByProduct(newProduct.getProduct());

		if (checkForDuplicate) {
			throw new ProductDuplicateCreationException(newProduct);
		}
		productRepository.save(newProduct);

		return new ProductResponse(
				newProduct.getId(),
				newProduct.getProduct(),
				newProduct.getPricePerPiece(),
				newProduct.getWeightPerPiece());
	}

	public void deleteProduct(Long id) {
		ProductEntity toDelete = getProductById(id);
		productRepository.delete(toDelete);
	}

	public Page<ProductResponse> getAllProducts(Pageable pageable) {
		Page<ProductEntity> items = productRepository.findAll(pageable);

		return items.map(productEntity -> new ProductResponse(
				productEntity.getId(),
				productEntity.getProduct(),
				productEntity.getPricePerPiece(),
				productEntity.getWeightPerPiece()
		));
	}

	public void updateProduct(Long id, ProductRequest productRequest) {
		ProductEntity productToUpdate = getProductById(id);
		productToUpdate.setProduct(productRequest.getProduct());
		productToUpdate.setPricePerPiece(productRequest.getValue());
		productRepository.save(productToUpdate);

	}


}


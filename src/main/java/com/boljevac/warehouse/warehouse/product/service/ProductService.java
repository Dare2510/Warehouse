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

	public ProductResponse createItem(ProductRequest productRequest) {
		ProductEntity newProductEntity = new ProductEntity(
				productRequest.getProduct(),
				productRequest.getValue(),
				productRequest.getWeight()
		);

		ProductEntity checkProduct = productRepository.findByProduct(productRequest.getProduct());
		if (checkProduct != null) {
			throw new ProductDuplicateCreationException(newProductEntity);
		}


		productRepository.save(newProductEntity);

		return new ProductResponse(newProductEntity.getId(),
				newProductEntity.getProduct(),
				newProductEntity.getPricePerPiece(),
				newProductEntity.getWeightPerPiece());
	}

	public void deleteItem(Long id) {
		ProductEntity toDelete = getProductById(id);
		productRepository.delete(toDelete);
	}

	public Page<ProductResponse> getAll(Pageable pageable) {
		Page<ProductEntity> items = productRepository.findAll(pageable);

		return items.map(productEntity -> new ProductResponse(
				productEntity.getId(),
				productEntity.getProduct(),
				productEntity.getPricePerPiece(),
				productEntity.getWeightPerPiece()
		));
	}
		//productEntity.getQuantity()
	public void updateProduct(Long id, ProductRequest productRequest) {
		ProductEntity toUpdate = getProductById(id);
		toUpdate.setProduct(productRequest.getProduct());
		//toUpdate.setQuantity(productRequest.getQuantity());
		toUpdate.setPricePerPiece(productRequest.getValue());
		productRepository.save(toUpdate);

	}


}


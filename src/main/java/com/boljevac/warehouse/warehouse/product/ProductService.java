package com.boljevac.warehouse.warehouse.product;

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

	public ProductResponse createItem(ProductRequest productRequest) {
		ProductEntity newProductEntity = new ProductEntity(
				productRequest.getProduct(),
				productRequest.getValue(),
				productRequest.getQuantity()
		);

		productRepository.save(newProductEntity);

		return new ProductResponse(newProductEntity.getId(), newProductEntity.getProduct(), newProductEntity.getValuePerPiece(), newProductEntity.getQuantity());
	}

	public void deleteItem(Long id) {
		ProductEntity toDelete = productRepository.findById(id).
				orElseThrow(() -> new ProductNotFoundException(id));

		productRepository.delete(toDelete);
	}

	public Page<ProductResponse> getAll(Pageable pageable){
		Page<ProductEntity> items = productRepository.findAll(pageable);

		return items.map(productEntity ->  new ProductResponse(
				productEntity.getId(), productEntity.getProduct(), productEntity.getValuePerPiece(), productEntity.getQuantity()
		));
	}

	public void updateProduct(Long id, ProductRequest productRequest) {
		ProductEntity toUpdate = productRepository.findById(id).orElseThrow(
				() -> new ProductNotFoundException(id)
		);
		toUpdate.setProduct(productRequest.getProduct());
		toUpdate.setQuantity(productRequest.getQuantity());
		toUpdate.setValuePerPiece(productRequest.getValue());
		productRepository.save(toUpdate);
		}



}


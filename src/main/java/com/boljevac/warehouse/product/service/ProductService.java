package com.boljevac.warehouse.product.service;

import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductService {

	private final UserService userService;
	ProductRepository productRepository;

	public ProductService(ProductRepository productRepository, UserService userService) {
		this.productRepository = productRepository;
		this.userService = userService;
	}

	public ProductEntity getProductById(Long id) throws ProductNotFoundException {
		return productRepository.findById(id).
				orElseThrow(() -> new ProductNotFoundException(id));
	}

	public ProductResponse createAndValidateNewProduct(AuthenticatedUser authenticatedUser,ProductRequest productRequest) {
		ProductEntity newProduct = new ProductEntity(
				productRequest.getProduct(),
				productRequest.getValue(),
				productRequest.getWeight()
		);

		boolean checkForDuplicate = productRepository.existsByProduct(newProduct.getProduct());
		UserEntity createdBy = userService.getUserByAuthenticatedUser(authenticatedUser);
		if (checkForDuplicate) {
			throw new ProductDuplicateCreationException(newProduct);
		}
		newProduct.setProductCreatedByUser(createdBy);
		productRepository.save(newProduct);
		log.info("New product with id {} created", newProduct.getId());

		return new ProductResponse(
				newProduct.getId(),
				newProduct.getProduct(),
				newProduct.getPricePerPiece(),
				newProduct.getWeightPerPiece(),
				newProduct.getProductCreatedByUser().getId()
		);
	}

	public void deleteProduct(Long id) {
		ProductEntity toDelete = getProductById(id);
		log.info("Product with id {} has been deleted", toDelete.getId());
		productRepository.delete(toDelete);
	}

	public Page<ProductResponse> getAllProducts(Pageable pageable) {
		Page<ProductEntity> items = productRepository.findAll(pageable);

		return items.map(productEntity -> new ProductResponse(
				productEntity.getId(),
				productEntity.getProduct(),
				productEntity.getPricePerPiece(),
				productEntity.getWeightPerPiece(),
				productEntity.getProductCreatedByUser().getId()
		));
	}

	public void updateProduct(AuthenticatedUser authenticatedUser,Long id, ProductRequest productRequest) {
		ProductEntity productToUpdate = getProductById(id);
		productToUpdate.setProduct(productRequest.getProduct());
		productToUpdate.setPricePerPiece(productRequest.getValue());
		productToUpdate.setWeightPerPiece(productRequest.getWeight());
		productToUpdate.setProductCreatedByUser(userService.getUserByAuthenticatedUser(authenticatedUser));
		productRepository.save(productToUpdate);
		log.info("Product with id {} has been updated", productToUpdate.getId());

	}


}


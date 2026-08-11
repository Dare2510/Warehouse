package com.boljevac.warehouse.product.service;

import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.product.dto.ProductRequest;
import com.boljevac.warehouse.product.dto.ProductResponse;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.exception.DeletionProductFailed;
import com.boljevac.warehouse.product.exception.ProductDuplicateCreationException;
import com.boljevac.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ProductService {

	private final UserService userService;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final ShippedOrdersRepository shippedOrdersRepository;
	private final InventoryRepository inventoryRepository;


	public ProductEntity getProductById(Long id) throws ProductNotFoundException {
		return productRepository.findById(id).
				orElseThrow(() -> new ProductNotFoundException(id));
	}

	public ProductResponse createAndValidateNewProduct(AuthenticatedUser authenticatedUser, ProductRequest productRequest) {
		UserEntity createdBy = userService.getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity newProduct = new ProductEntity(
				createdBy,
				productRequest.getProduct(),
				productRequest.getValue(),
				productRequest.getWeight()
		);

		boolean checkForDuplicate = productRepository.existsByProduct(newProduct.getProduct());

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
		boolean orderExists = orderRepository.existsByProductEntity(toDelete) ||
				shippedOrdersRepository.existsByProductId(toDelete.getId());

		boolean inventoryExists = inventoryRepository.existsByProductEntity(toDelete);

		if (!orderExists && !inventoryExists) {
			log.info("Product with id {} has been deleted", toDelete.getId());
			productRepository.delete(toDelete);

		} else {
			log.info("Product with id {} cannot be deleted, order or inventory exists", toDelete.getId());
			throw new DeletionProductFailed(toDelete.getId());
		}
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

	public void updateProduct(AuthenticatedUser authenticatedUser, Long id, ProductRequest productRequest) {
		ProductEntity productToUpdate = getProductById(id);
		productToUpdate.setProduct(productRequest.getProduct());
		productToUpdate.setPricePerPiece(productRequest.getValue());
		productToUpdate.setWeightPerPiece(productRequest.getWeight());
		productToUpdate.setProductCreatedByUser(userService.getUserByAuthenticatedUser(authenticatedUser));
		productRepository.save(productToUpdate);
		log.info("Product with id {} has been updated", productToUpdate.getId());

	}


}


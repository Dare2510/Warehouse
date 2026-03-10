package com.boljevac.warehouse.warehouse.inventory.service;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;

	public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;

	}

	public InventoryResponse getStock(Long id) {
		InventoryEntity stockProduct = inventoryRepository.findById(id).orElseThrow(
				() -> new ProductNotFoundException(id)
		);
		return new InventoryResponse(inventoryRepository.getByProductEntity(
				stockProduct.getProductEntity()).getProductEntity().getProduct(),
				stockProduct.getQuantity());

	}

	@Transactional
	public InventoryResponse createStock(InventoryRequest inventoryRequest) {
		ProductEntity product = productRepository.findById(inventoryRequest.getProductId()).orElseThrow(
				() -> new ProductNotFoundException(inventoryRequest.getProductId())
		);
		InventoryEntity existingInventoryProduct = inventoryRepository.findById(product.getId()).orElse(null);

		if (existingInventoryProduct != null) {
			int currentQuantity = inventoryRepository.getByProductEntity(product).getQuantity();
			existingInventoryProduct.setQuantity(currentQuantity + inventoryRequest.getQuantity());
			inventoryRepository.save(existingInventoryProduct);
		}

		InventoryEntity newInventoryProduct = new InventoryEntity(
				product, inventoryRequest.getQuantity(), "Block storage"
		);

		inventoryRepository.save(newInventoryProduct);

		return new InventoryResponse(product.getProduct(), newInventoryProduct.getQuantity());

	}

}

package com.boljevac.warehouse.warehouse.inventory.service;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.*;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.exception.ProductNotFoundException;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final ProductRepository productRepository;

	public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
		this.inventoryRepository = inventoryRepository;
		this.productRepository = productRepository;
	}

	public InventoryEntity getInventoryProduct(ProductEntity productEntity) {
	return inventoryRepository.findById(productEntity.getId()).orElseThrow(
			() ->  new ProductNotFoundException(productEntity.getId())
	);
	}

	public InventoryResponse storeProduct(InventoryRequest inventoryRequest) {

		ProductEntity productToStore = productRepository.findById(inventoryRequest.getProductId()).orElseThrow(
				()-> new ProductNotFoundException(inventoryRequest.getProductId())
		);

		InventoryEntity inventory = new InventoryEntity(
				productToStore,
				inventoryRequest.getAisle().toString(),
				inventoryRequest.getRack(),
				inventoryRequest.getLevelOn(),
				inventoryRequest.getQuantity()
				);

		inventoryRepository.save(inventory);

		Location location = new Location(
				inventoryRequest.getAisle(),
				new Rack(inventoryRequest.getRack()),
				new LevelOn(inventoryRequest.getLevelOn())
		);

		return new InventoryResponse(inventory.getProductEntity().getProduct(),location.toString(), inventory.getQuantity());

	}

	public List<InventoryResponse> getInventory(){
		return inventoryRepository.findAll().stream()
				.filter(inventory -> inventory.getProductEntity().getProduct() != null)
				.map(product ->
						new InventoryResponse(
								product.getProductEntity().getProduct(),
								product.getAisle() + " "
								+ product.getRack() + " "
								+ product.getLevelOn(),
								product.getQuantity()
						)).toList();

	}
}

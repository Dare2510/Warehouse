package com.boljevac.warehouse.warehouse.inventory.service;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final LocationsRepository  locationsRepository;
	private final ProductService productService;
	private final Logger logger = LoggerFactory.getLogger(InventoryService.class);

	public InventoryService(InventoryRepository inventoryRepository,
							LocationsRepository locationsRepository, ProductService productService) {
		this.inventoryRepository = inventoryRepository;
		this.locationsRepository = locationsRepository;
		this.productService = productService;
	}
	@Transactional
	public InventoryResponse getInventoryResponse(Long id) {
		InventoryEntity inventory = getInventoryEntity(id);

		return new InventoryResponse(
				inventory.getProductEntity().getProduct(),
				inventory.getQuantity());

	}

	@Transactional
	public InventoryResponse createStock(InventoryRequest inventoryRequest) {
		ProductEntity product = productService.getProductById(inventoryRequest.getProductId());

		LocationEntity newLocation = new LocationEntity(
										product,
										LocationType.BLOCK,
										inventoryRequest.getQuantity(),
										true);

		locationsRepository.save(newLocation);

		InventoryEntity newInventoryProduct = new InventoryEntity(
										product,
										newLocation,
										inventoryRequest.getQuantity(),
										newLocation.toString()
		);
		inventoryRepository.save(newInventoryProduct);

		logger.info("New Location with Id {} and new Inventory with Id {} have been created",
				newLocation.getId(), newInventoryProduct.getId());

		return new InventoryResponse(product.getProduct(), newInventoryProduct.getQuantity());

	}

	private InventoryEntity getInventoryEntity(Long id) {
		return inventoryRepository.findById(id).orElseThrow(
				() -> new InventoryNotFoundException(id)
		);
	}
}

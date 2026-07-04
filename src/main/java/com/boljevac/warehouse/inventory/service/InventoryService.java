package com.boljevac.warehouse.inventory.service;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.location.exceptions.LocationsNotCreatedException;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.service.ProductService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InventoryService {

	private final InventoryRepository inventoryRepository;
	private final LocationsRepository locationsRepository;
	private final ProductService productService;
	private final UserService userService;

	public InventoryService(InventoryRepository inventoryRepository,
	                        LocationsRepository locationsRepository, ProductService productService, UserService userService) {
		this.inventoryRepository = inventoryRepository;
		this.locationsRepository = locationsRepository;
		this.productService = productService;
		this.userService = userService;
	}

	@Transactional
	public InventoryResponse getInventoryResponse(Long id) {
		InventoryEntity inventory = getInventoryEntity(id);

		return new InventoryResponse(
				inventory.getProductEntity().getProduct(),
				inventory.getQuantity());

	}

	@Transactional
	public InventoryResponse createStock(AuthenticatedUser authenticatedUser,InventoryRequest inventoryRequest) {
		//First creation of Locations is needed
		if (!validateLocationsExists()) {
			log.warn("Locations not created");
			throw new LocationsNotCreatedException();
		}

		ProductEntity product = productService.getProductById(inventoryRequest.getProductId());

		LocationEntity newLocation = new LocationEntity(
				product,
				LocationType.BLOCK,
				inventoryRequest.getQuantity(),
				true);

		UserEntity stockCreatedBy = userService.getUserByAuthenticatedUser(authenticatedUser);
		newLocation.setLocationCreatedByUser(stockCreatedBy);
		locationsRepository.save(newLocation);

		InventoryEntity newInventoryProduct = new InventoryEntity(
				product,
				newLocation,
				inventoryRequest.getQuantity(),
				newLocation.toString()
		);

		newInventoryProduct.setCreatedByUser(stockCreatedBy);
		inventoryRepository.save(newInventoryProduct);

		log.info("New Location with Id {} and new Inventory with Id {} have been created",
				newLocation.getId(), newInventoryProduct.getId());

		return new InventoryResponse(product.getProduct(), newInventoryProduct.getQuantity());

	}

	private InventoryEntity getInventoryEntity(Long id) {
		return inventoryRepository.findById(id).orElseThrow(
				() -> new InventoryNotFoundException(id)
		);
	}

	//Helper Methods
	private boolean validateLocationsExists() {
		return locationsRepository.count() < 1 ? false : true;
	}
}

package com.boljevac.warehouse.warehouse.location.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.entity.Aisle;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationLoadLimitExceededException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationsAlreadyCreatedException;
import com.boljevac.warehouse.warehouse.location.exceptions.NoUnusedLocationException;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LocationService {
	private static final double MAX_LOCATION_WEIGHT = 1000;
	private final LocationsRepository locationsRepository;
	private final InventoryRepository inventoryRepository;

	public LocationService(LocationsRepository locationsRepository, InventoryRepository inventoryRepository) {
		this.locationsRepository = locationsRepository;
		this.inventoryRepository = inventoryRepository;
	}

	public void createLocations() {
		final int firstRack = 1;
		final int lastRack = 10;
		final int minLevel = 1;
		final int maxLevel = 6;
		if (locationsRepository.count() > 0) {
			throw new LocationsAlreadyCreatedException();
		}
		for (Aisle aisle : Aisle.values()) {
			if (aisle.name().equalsIgnoreCase(Aisle.Floor.toString())) {
				continue;
			}
			for (int rack = firstRack; rack <= lastRack; rack++) {
				for (int level = minLevel; level <= maxLevel; level++) {
					LocationEntity locationEntity = new LocationEntity();
					locationEntity.setAisle(aisle.name());
					locationEntity.setRack(rack);
					locationEntity.setLevel(level);
					locationEntity.setLoaded(false);
					locationEntity.setQuantity(0);
					locationEntity.setProductEntity(null);
					locationEntity.setRemainingWeightToStore(MAX_LOCATION_WEIGHT);
					locationEntity.setLocationType(LocationType.STORAGE);
					locationsRepository.save(locationEntity);
				}
			}
		}
	}

	@Transactional
	public LocationsResponse storeInventory(LocationsRequest toStoreRequest) {

		InventoryEntity toStoreFrom = getEntityToStoreFrom(toStoreRequest);
		LocationEntity fromLocation = toStoreFrom.getLocationEntity();
		LocationEntity toStoreInLocation = getAvailableLocation();
		ProductEntity product = toStoreFrom.getProductEntity();

		int quantityToStore = toStoreRequest.getQuantity();
		int availableQuantity = toStoreFrom.getQuantity();
		double weightPerPiece = product.getWeightPerPiece();
		double weightToStore = weightPerPiece * quantityToStore;
		double availableWeightOnLocation = toStoreInLocation.getRemainingWeightToStore();
		double toStoreFromWeight = toStoreFrom.getTotalWeight();

		validateAvailableQuantity(quantityToStore, availableQuantity);
		validateLocationWeight(weightToStore, availableWeightOnLocation, toStoreInLocation.getId());

		updateFromInventory(weightToStore, toStoreFromWeight, availableQuantity, quantityToStore, toStoreFrom, fromLocation);
		updateTargetLocation(quantityToStore, weightToStore, product, toStoreInLocation);

		InventoryEntity storedInventory = new InventoryEntity(
				product,
				toStoreInLocation,
				toStoreInLocation.getQuantity(),
				toStoreInLocation.toString());

		saveEntities(fromLocation, toStoreInLocation, toStoreFrom, storedInventory);

		return mapToResponse(toStoreInLocation, product, quantityToStore);
	}

	@Transactional
	public Page<LocationsResponse> getInventories(Pageable pageable) {
		Page<LocationEntity> locationsPage = locationsRepository.findByProductEntityIsNotNull(pageable);

		return locationsPage.map(location -> new LocationsResponse(
				location.getId(),
				location.getProductEntity().getProduct(),
				location.getProductEntity().getWeightPerPiece(),
				location.getQuantity() * location.getProductEntity().getWeightPerPiece(),
				location.toString()));
	}

	// Helper Methods

	private InventoryEntity getEntityToStoreFrom(LocationsRequest locationsRequest) {

		return inventoryRepository.findById(locationsRequest.getInventoryId()).orElseThrow(()
				-> new InventoryNotFoundException(locationsRequest.getInventoryId()));
	}

	private LocationEntity getAvailableLocation() {
		Long availableLocationID = getAvailableLocationID();
		return locationsRepository.getLocationById(availableLocationID);
	}

	private Long getAvailableLocationID() {
		for (LocationEntity location : locationsRepository.findAll()) {
			if (!location.isLoaded()) {
				return location.getId();
			}
		}
		throw new NoUnusedLocationException();
	}

	private void validateAvailableQuantity(int quantityToStore, int availableQuantity) {
		if (availableQuantity < quantityToStore) {
			throw new NotSufficientStockToStoreException(quantityToStore);
		}
	}

	private void validateLocationWeight(double weightToStore, double availableWeightToStore, Long locationId) {
		if (weightToStore > availableWeightToStore) {
			throw new LocationLoadLimitExceededException(locationId);
		}
	}

	private void updateTargetLocation(int quantityToStore, double weightToStore, ProductEntity product,
									  LocationEntity toStoreInLocation) {
		toStoreInLocation.setLoaded(true);
		toStoreInLocation.setProductEntity(product);
		toStoreInLocation.setQuantity(quantityToStore);
		toStoreInLocation.setRemainingWeightToStore(toStoreInLocation.getRemainingWeightToStore() - weightToStore);

		if (toStoreInLocation.getId() > 300) {
			toStoreInLocation.setLocationType(LocationType.BLOCK);
		} else {
			toStoreInLocation.setLocationType(LocationType.STORAGE);
		}
	}

	private void updateFromInventory(double weightToStore, double toStoreFromWeight,
									 int availableQuantity, int quantityToStore,
									 InventoryEntity toStoreFrom, LocationEntity fromLocation) {

		if (availableQuantity == quantityToStore) {
			toStoreFrom.setTotalWeight(0);
			toStoreFrom.setQuantity(0);
			toStoreFrom.setProductEntity(null);
			toStoreFrom.setLocationEntity(null);

			fromLocation.setLoaded(false);
			fromLocation.setRemainingWeightToStore(MAX_LOCATION_WEIGHT);
			fromLocation.setProductEntity(null);
			fromLocation.setQuantity(0);
			return;
		}
		toStoreFrom.setQuantity(availableQuantity - quantityToStore);
		toStoreFrom.setTotalWeight(toStoreFromWeight - weightToStore);
		fromLocation.setQuantity(fromLocation.getQuantity() - quantityToStore);
		fromLocation.setRemainingWeightToStore(fromLocation.getRemainingWeightToStore() + weightToStore);
	}

	private void saveEntities(LocationEntity fromLocation, LocationEntity toStoreInLocation,
							  InventoryEntity toStoreFrom, InventoryEntity storedInventory) {
		locationsRepository.save(fromLocation);
		locationsRepository.save(toStoreInLocation);
		inventoryRepository.save(toStoreFrom);
		inventoryRepository.save(storedInventory);
	}

	private LocationsResponse mapToResponse(LocationEntity location,
											ProductEntity product, int quantityToStore) {
		return new LocationsResponse(
				location.getId(),
				product.toString(),
				product.getWeightPerPiece(),
				product.getWeightPerPiece() * quantityToStore,
				location.toString());
	}
}

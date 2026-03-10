package com.boljevac.warehouse.warehouse.location.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.Aisle;
import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.location.exceptions.NoUnusedLocationException;
import com.boljevac.warehouse.warehouse.location.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

	private final LocationsRepository locationsRepository;
	private final InventoryRepository inventoryRepository;

	public LocationService(LocationsRepository locationsRepository, InventoryRepository inventoryRepository) {
		this.locationsRepository = locationsRepository;
		this.inventoryRepository = inventoryRepository;
	}

	public void createLocations() {
		final int firstRack = 1;
		final int lastRack = 10;

		final int maxLevel = 6;
		final int minLevel = 1;

		for (Aisle aisle : Aisle.values()) {
			for (int i = firstRack; i <= lastRack; i++) {
				for (int j = minLevel; j <= maxLevel; j++) {
					LocationEntity locationEntity = new LocationEntity();
					locationEntity.setRack(i);
					locationEntity.setLevel(j);
					locationEntity.setAisle(aisle.name());
					locationEntity.setLoaded(false);
					locationsRepository.save(locationEntity);
				}
			}
		}

	}

	public LocationsResponse storeInventory(LocationsRequest locationsRequest) {
		InventoryEntity toStore =
				inventoryRepository.findById(locationsRequest.getInventoryId()).orElseThrow(
						() -> new InventoryNotFoundException(locationsRequest.getInventoryId())
				);
		Long nextUnusedId = 0L;

		for (LocationEntity location : locationsRepository.findAll()) {
			if (!location.isLoaded()) {
				nextUnusedId = location.getId();
				break;
			}
		}
		if (nextUnusedId == 0L) {
			throw new NoUnusedLocationException();
		}
		if (toStore.getQuantity() < locationsRequest.getQuantity()) {
			throw new NotSufficientStockToStoreException(locationsRequest.getQuantity());
		}
		toStore.setQuantity(toStore.getQuantity() - locationsRequest.getQuantity());
		inventoryRepository.save(toStore);
		if (toStore.getQuantity() == 0) {
			inventoryRepository.deleteById(toStore.getId());
		}

		LocationEntity toStoreInLocation = locationsRepository.getLocationById(nextUnusedId);
		toStoreInLocation.setLoaded(true);
		toStoreInLocation.setProductEntity(toStore.getProductEntity());
		toStoreInLocation.setQuantity(locationsRequest.getQuantity());
		locationsRepository.save(toStoreInLocation);

		InventoryEntity inventoryEntity = new InventoryEntity(
				toStoreInLocation.getProductEntity(),
				toStoreInLocation.getQuantity(),
				toStoreInLocation.toString()
		);
		inventoryRepository.save(inventoryEntity);
		return new LocationsResponse(toStoreInLocation.getId(), true);

	}
}

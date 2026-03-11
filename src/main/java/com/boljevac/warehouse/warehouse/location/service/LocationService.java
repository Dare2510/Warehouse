package com.boljevac.warehouse.warehouse.location.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.entity.Aisle;
import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationLoadLimitExceededException;
import com.boljevac.warehouse.warehouse.location.exceptions.LocationsAlreadyCreatedException;
import com.boljevac.warehouse.warehouse.location.exceptions.NoUnusedLocationException;
import com.boljevac.warehouse.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import jakarta.transaction.Transactional;
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

		final int minLevel = 1;
		final int maxLevel = 6;

		for (Aisle aisle : Aisle.values()) {
			for (int i = firstRack; i <= lastRack; i++) {
				for (int j = minLevel; j <= maxLevel; j++) {
					LocationEntity locationEntity = new LocationEntity();

					locationEntity.setAisle(aisle.name());
					locationEntity.setRack(i);
					locationEntity.setLevel(j);

					locationEntity.setLoaded(false);
					locationsRepository.save(locationEntity);
					if(locationEntity.getId()>300){
						locationsRepository.delete(locationEntity);
						throw new LocationsAlreadyCreatedException();
					}

				}
			}
		}

	}
	@Transactional
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

		if(toStore.getTotalWeight()> locationsRepository.getLocationById(nextUnusedId).getRemainingWeightToStore()){
			throw new LocationLoadLimitExceededException(nextUnusedId);
		}
		toStore.setQuantity(toStore.getQuantity() - locationsRequest.getQuantity());
		inventoryRepository.save(toStore);
		if (toStore.getQuantity() == 0) {
			inventoryRepository.deleteById(toStore.getId());
		}

		double weight = toStore.getProductEntity().getWeightPerPiece();
		int quantity = locationsRequest.getQuantity();

		LocationEntity toStoreInLocation = locationsRepository.getLocationById(nextUnusedId);

		toStoreInLocation.setLoaded(true);
		toStoreInLocation.setProductEntity(toStore.getProductEntity());
		toStoreInLocation.setQuantity(locationsRequest.getQuantity());
		toStoreInLocation.setRemainingWeightToStore(
				toStoreInLocation.getRemainingWeightToStore()-(weight * quantity));
		locationsRepository.save(toStoreInLocation);

		InventoryEntity inventoryEntity = new InventoryEntity(
				toStoreInLocation.getProductEntity(),
				toStoreInLocation.getQuantity(),
				toStoreInLocation.toString()
		);

		toStore.setTotalWeight(toStore.getTotalWeight()-(weight * quantity));
		inventoryRepository.save(toStore);

		inventoryRepository.save(inventoryEntity);
		return new LocationsResponse(toStoreInLocation.getId(), toStoreInLocation.toString());

	}
}

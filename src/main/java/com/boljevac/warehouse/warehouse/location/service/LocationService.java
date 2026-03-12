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
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public LocationsResponse storeInventory(LocationsRequest toStoreRequest) {

		InventoryEntity toStoreFrom = getEntityToStoreFrom(toStoreRequest);
		Long freeLocationId = getAvailableLocationID();
		LocationEntity toStoreInLocation = getLocationEntity();



		double availableWeightOnLocation = toStoreInLocation.getRemainingWeightToStore();
		int availableQuantity = toStoreFrom.getQuantity();
		int quantityToStore = toStoreRequest.getQuantity();

		double toStoreFromWeight = toStoreFrom.getTotalWeight();
		double weightToStore = toStoreFrom.getProductEntity().getWeightPerPiece()*quantityToStore;

		validateAvailableQuantity(toStoreRequest, toStoreFrom);
		validateLocationWeight(weightToStore,availableWeightOnLocation,freeLocationId);

		setterForFromLocation(weightToStore,toStoreFromWeight,availableQuantity,quantityToStore,toStoreFrom);
		inventoryRepository.save(toStoreFrom);

		setterForLocation(quantityToStore, weightToStore, toStoreFrom, toStoreInLocation);

		locationsRepository.save(toStoreInLocation);

		InventoryEntity inventoryEntity = new InventoryEntity(
				toStoreInLocation.getProductEntity(),
				toStoreInLocation.getQuantity(),
				toStoreInLocation.toString()
		);

		inventoryRepository.save(toStoreFrom);
		inventoryRepository.save(inventoryEntity);


		return new LocationsResponse(
				toStoreInLocation.getId(),
				toStoreInLocation.getProductEntity().getProduct(),
				toStoreInLocation.getProductEntity().getWeightPerPiece(),
				toStoreInLocation.getProductEntity().getWeightPerPiece()*quantityToStore,
				toStoreInLocation.toString());

	}
	@Transactional
	public Page<LocationsResponse> getInventories(Pageable pageable) {
		Page<LocationEntity> locationsPage = locationsRepository.findByProductEntityIsNotNull(pageable);

		return locationsPage.map(location -> new LocationsResponse(
				location.getId(),
				location.getProductEntity().getProduct(),
				location.getProductEntity().getWeightPerPiece(),
				location.getQuantity()*location.getProductEntity().getWeightPerPiece(),
				location.toString()
		));
	}

	//Helper Methods
	private InventoryEntity getEntityToStoreFrom(LocationsRequest locationsRequest) {
		return inventoryRepository.findById(locationsRequest.getInventoryId()).orElseThrow(
				() -> new InventoryNotFoundException(locationsRequest.getInventoryId())
		);
	}

	private Long getAvailableLocationID(){
		Long availableLocationID = 0L;
		for (LocationEntity location : locationsRepository.findAll()) {
			if (!location.isLoaded()) {
				availableLocationID = location.getId();
				break;
			}
		}
		if (availableLocationID == 0L) {
			throw new NoUnusedLocationException();
		}
		return availableLocationID;

	}

	private void validateAvailableQuantity(LocationsRequest locationsRequest,InventoryEntity toStore) {
		if (toStore.getQuantity() < locationsRequest.getQuantity()) {
			throw new NotSufficientStockToStoreException(locationsRequest.getQuantity());
		}
	}

	private void validateLocationWeight(double weightToStore, double availableWeightToStore, Long locationId) {
		if(weightToStore> availableWeightToStore){
			throw new LocationLoadLimitExceededException(locationId);
		}
	}

	private LocationEntity getLocationEntity() {
		Long  availableLocationID = getAvailableLocationID();

		return locationsRepository.getLocationById(availableLocationID);
	}

	private void setterForLocation(int quantity, double weight, InventoryEntity toStoreFrom, LocationEntity toStoreInLocation) {
		toStoreInLocation.setLoaded(true);
		toStoreInLocation.setProductEntity(toStoreFrom.getProductEntity());
		toStoreInLocation.setQuantity(quantity);
		toStoreInLocation.setRemainingWeightToStore(toStoreInLocation.getRemainingWeightToStore()-weight);
	}

	private void setterForFromLocation(double weightToStore, double toStoreFromWeight,
									   int availableQuantity, int toStoreQuantity, InventoryEntity toStoreFrom) {
	if(availableQuantity == toStoreQuantity){
		toStoreFrom.setTotalWeight(0);
		toStoreFrom.setQuantity(0);
		toStoreFrom.setProductEntity(null);

		//todo LocationEntity updates
	}
		toStoreFrom.setQuantity(availableQuantity - toStoreQuantity);
		toStoreFrom.setTotalWeight(toStoreFromWeight-(weightToStore));
	}

}

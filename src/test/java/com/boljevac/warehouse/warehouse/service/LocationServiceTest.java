package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.location.service.LocationService;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

	@Mock
	private LocationsRepository  locationsRepository;

	@Mock
	private InventoryRepository inventoryRepository;

	@InjectMocks
	private LocationService locationService;

	private ProductEntity createProductHelper() {
		return new ProductEntity("TestProduct", BigDecimal.TEN, 10);
	}
	private LocationEntity createLocationHelper(ProductEntity product) {
		return new LocationEntity(product, LocationType.BLOCK,20,true);
	}

	private InventoryEntity createInventoryHelper(ProductEntity product, LocationEntity locationEntity, String location) {
		return new InventoryEntity(product, locationEntity, 20, location);
	}

	@Test
	public void create_locations_success() {
		locationService.createLocations();
		verify(locationsRepository, times(300)).save(any());
	}

	@Test
	public void store_subset_success() {

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L,5);

		ProductEntity product = createProductHelper();
		LocationEntity toStoreFromLocation = createLocationHelper(product);
		InventoryEntity toStoreFromInventory = createInventoryHelper(product,toStoreFromLocation,toStoreFromLocation.toString());
		toStoreFromLocation.setLocationType(LocationType.STORAGE);
		toStoreFromLocation.setId(5L);
		toStoreFromLocation.setRemainingWeightToStore(800);


		Long toStoreInId = 10L;
		LocationEntity toStoreInLocation = createLocationHelper(product);
		toStoreInLocation.setId(toStoreInId);
		toStoreInLocation.setRemainingWeightToStore(1000);

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(toStoreFromInventory));
		when(locationsRepository.findAll()).thenReturn(Collections.singletonList(toStoreInLocation));
		when(locationsRepository.getLocationById(toStoreInLocation.getId())).thenReturn(toStoreInLocation);

		locationService.storeInventory(request);

		verify(inventoryRepository, times(2)).save(any());
		verify(locationsRepository, times(302)).save(any());

		assertEquals(5L,toStoreFromInventory.getLocationEntity().getId());
		assertEquals(15,toStoreFromInventory.getQuantity());
		assertEquals("TestProduct", toStoreFromInventory.getProductEntity().getProduct());

		assertEquals(850,toStoreFromLocation.getRemainingWeightToStore());
		assertEquals(15,toStoreFromLocation.getQuantity());
		assertEquals("TestProduct", toStoreFromLocation.getProductEntity().getProduct());

		assertTrue(toStoreInLocation.isLoaded());
		assertEquals(950,toStoreInLocation.getRemainingWeightToStore());
		assertEquals("TestProduct", toStoreInLocation.getProductEntity().getProduct());
		assertEquals(5,toStoreInLocation.getQuantity());


	}


}

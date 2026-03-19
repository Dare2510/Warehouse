package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.inventory.service.InventoryService;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

	@Mock
	InventoryRepository inventoryRepository;
	@Mock
	LocationsRepository locationsRepository;
	@Mock
	ProductService productService;

	@InjectMocks
	InventoryService inventoryService;

	private ProductEntity createProductHelper() {
		return new ProductEntity("TestProduct", BigDecimal.TEN, 100);
	}
	private LocationEntity createLocationHelper(ProductEntity product) {
		return new LocationEntity(product, LocationType.BLOCK,20,true);
	}

	private InventoryEntity createInventoryHelper(ProductEntity product, LocationEntity locationEntity, String location) {
		return new InventoryEntity(product, locationEntity, 20, location);
	}

	@Test
	public void get_inventory_by_id() {
		ProductEntity product = createProductHelper();
		LocationEntity locationEntity = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,locationEntity,locationEntity.toString());

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
		InventoryResponse response = inventoryService.getInventoryResponse(1L);

		assertEquals("TestProduct", response.product());
		assertEquals(20,response.quantity());

	}

	@Test
	public void get_inventory_by_id_throws() {

		when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(InventoryNotFoundException.class, () -> {
			inventoryService.getInventoryResponse(1L);
		});

	}

	@Test
	public void create_stock(){
		InventoryRequest request = new InventoryRequest(1L,20);
		ProductEntity product = createProductHelper();
		when(productService.getProductById(1L)).thenReturn(product);

		inventoryService.createStock(request);

		verify(locationsRepository).save(any(LocationEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

	}


}

package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.inventory.exceptions.NotSufficientStockToStoreException;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.location.exceptions.LocationLoadLimitExceededException;
import com.boljevac.warehouse.location.exceptions.LocationsAlreadyCreatedException;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.location.service.LocationService;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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
	private LocationsRepository locationsRepository;

	@Mock
	private InventoryRepository inventoryRepository;

	@Mock
	private UserService  userService;

	@InjectMocks
	private LocationService locationService;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("10");
	private static final double PRODUCT_WEIGHT = 10;

	private static final LocationType LOCATION_TYPE = LocationType.BLOCK;
	private static final int LOCATION_QUANTITY = 20;
	private static final boolean LOCATION_USED = true;

	private static final int INVENTORY_QUANTITY = 20;

	@BeforeEach
	void setUp() {
		locationService = new LocationService(locationsRepository,inventoryRepository,userService);
	}

	@Test
	public void createLocations_whenLocationsAreFirstTimeCreated_creates300Locations() {
		locationService.createLocations();
		verify(locationsRepository, times(300)).save(any());
	}

	@Test
	public void createLocations_whenLocationsAlreadyExist_throwsLocationsAlreadyCreatedException() {
		when(locationsRepository.count()).thenReturn(5L);
		assertThrows(LocationsAlreadyCreatedException.class,
				() -> locationService.createLocations());

		verify(locationsRepository, never()).save(any());
	}

	@Test
	public void storeInventory_whenSubsetToStoreMeetsAllRequirements_returnsLocationsResponse() {

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L, 5);

		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity toStoreFromLocation = createLocationHelper(product);
		InventoryEntity toStoreFromInventory = createInventoryHelper(product, toStoreFromLocation, toStoreFromLocation.toString());
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

		locationService.storeInventory(authenticatedUser,request);

		verify(inventoryRepository, times(2)).save(any());
		verify(locationsRepository, times(302)).save(any());

		assertEquals(5L, toStoreFromInventory.getLocationEntity().getId());
		assertEquals(15, toStoreFromInventory.getQuantity());
		assertEquals(PRODUCT_NAME, toStoreFromInventory.getProductEntity().getProduct());

		assertEquals(850, toStoreFromLocation.getRemainingWeightToStore());
		assertEquals(15, toStoreFromLocation.getQuantity());
		assertEquals(PRODUCT_NAME, toStoreFromLocation.getProductEntity().getProduct());

		assertTrue(toStoreInLocation.isLoaded());
		assertEquals(950, toStoreInLocation.getRemainingWeightToStore());
		assertEquals(PRODUCT_NAME, toStoreInLocation.getProductEntity().getProduct());
		assertEquals(5, toStoreInLocation.getQuantity());
	}

	@Test
	public void storeInventory_whenEntireStorageUnitMeetsAllRequirements_returnsLocationsResponse() {

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L, 5);

		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity toStoreFromLocation = createLocationHelper(product);
		InventoryEntity toStoreFromInventory = createInventoryHelper(product, toStoreFromLocation, toStoreFromLocation.toString());
		toStoreFromLocation.setQuantity(5);
		toStoreFromLocation.setLocationType(LocationType.STORAGE);
		toStoreFromLocation.setId(5L);
		toStoreFromLocation.setRemainingWeightToStore(800);
		toStoreFromInventory.setQuantity(5);

		Long toStoreInId = 10L;
		LocationEntity toStoreInLocation = createLocationHelper(product);
		toStoreInLocation.setId(toStoreInId);
		toStoreInLocation.setRemainingWeightToStore(1000);

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(toStoreFromInventory));
		when(locationsRepository.findAll()).thenReturn(Collections.singletonList(toStoreInLocation));
		when(locationsRepository.getLocationById(toStoreInLocation.getId())).thenReturn(toStoreInLocation);

		locationService.storeInventory(authenticatedUser,request);

		verify(inventoryRepository, times(2)).save(any());
		verify(locationsRepository, times(302)).save(any());

		assertNotNull(toStoreFromInventory.getLocationEntity());
		assertEquals(0, toStoreFromInventory.getQuantity());
		assertNull(toStoreFromInventory.getProductEntity());

		assertEquals(1000, toStoreFromLocation.getRemainingWeightToStore());
		assertEquals(0, toStoreFromLocation.getQuantity());
		assertNull(toStoreFromLocation.getProductEntity());
		assertFalse(toStoreFromLocation.isLoaded());

		assertTrue(toStoreInLocation.isLoaded());
		assertEquals(950, toStoreInLocation.getRemainingWeightToStore());
		assertEquals(PRODUCT_NAME, toStoreInLocation.getProductEntity().getProduct());
		assertEquals(5, toStoreInLocation.getQuantity());
	}

	@Test
	public void storeInventory_whenStockIsNotSufficient_throwsNotSufficientStockToStoreException() {

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L, 10);

		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity toStoreFromLocation = createLocationHelper(product);
		InventoryEntity toStoreFromInventory = createInventoryHelper(product, toStoreFromLocation, toStoreFromLocation.toString());
		toStoreFromLocation.setQuantity(5);
		toStoreFromLocation.setLocationType(LocationType.STORAGE);
		toStoreFromLocation.setId(5L);
		toStoreFromLocation.setRemainingWeightToStore(800);
		toStoreFromInventory.setQuantity(5);

		Long toStoreInId = 10L;
		LocationEntity toStoreInLocation = createLocationHelper(product);
		toStoreInLocation.setId(toStoreInId);
		toStoreInLocation.setRemainingWeightToStore(1000);

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(toStoreFromInventory));
		when(locationsRepository.findAll()).thenReturn(Collections.singletonList(toStoreInLocation));
		when(locationsRepository.getLocationById(toStoreInLocation.getId())).thenReturn(toStoreInLocation);

		assertThrows(NotSufficientStockToStoreException.class,
				() -> locationService.storeInventory(authenticatedUser,request));

		verify(locationsRepository, never()).save(toStoreInLocation);
	}

	@Test
	public void storeInventory_whenLocationsRemainingWeightToStoreIsNotEnough_throwsLocationLoadLimitExceededException() {

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L, 5);

		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity toStoreFromLocation = createLocationHelper(product);
		InventoryEntity toStoreFromInventory = createInventoryHelper(product, toStoreFromLocation, toStoreFromLocation.toString());
		toStoreFromLocation.setLocationType(LocationType.STORAGE);
		toStoreFromLocation.setId(5L);
		toStoreFromLocation.setRemainingWeightToStore(800);

		Long toStoreInId = 10L;
		LocationEntity toStoreInLocation = createLocationHelper(product);
		toStoreInLocation.setId(toStoreInId);
		toStoreInLocation.setRemainingWeightToStore(0);

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(toStoreFromInventory));
		when(locationsRepository.findAll()).thenReturn(Collections.singletonList(toStoreInLocation));
		when(locationsRepository.getLocationById(toStoreInLocation.getId())).thenReturn(toStoreInLocation);

		assertThrows(LocationLoadLimitExceededException.class,
				() -> locationService.storeInventory(authenticatedUser,request));

		verify(locationsRepository, never()).save(toStoreInLocation);

	}

	@Test
	public void storeInventory_whenInventoryWasNotFound_throwsInventoryNotFoundException() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();

		locationService.createLocations();
		LocationsRequest request = new LocationsRequest(1L, 5);
		assertThrows(InventoryNotFoundException.class, ()
				-> locationService.storeInventory(authenticatedUser,request));

		verify(inventoryRepository, times(1)).findById(anyLong());

	}

	private AuthenticatedUser createAuthenticatedAdminHelper(){
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser){
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}

	private ProductEntity createProductHelper(UserEntity user){
		return new ProductEntity(user,PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}

	private LocationEntity createLocationHelper(ProductEntity product) {
		return new LocationEntity(product, LOCATION_TYPE, LOCATION_QUANTITY, LOCATION_USED);
	}

	private InventoryEntity createInventoryHelper(ProductEntity product, LocationEntity locationEntity, String location) {
		return new InventoryEntity(product, locationEntity, INVENTORY_QUANTITY, location);
	}
}

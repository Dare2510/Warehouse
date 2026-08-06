package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.inventory.dto.InventoryRequest;
import com.boljevac.warehouse.inventory.dto.InventoryResponse;
import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.exceptions.InventoryNotFoundException;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.inventory.service.InventoryService;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.location.exceptions.LocationsNotCreatedException;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.service.ProductService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

	@Mock
	InventoryRepository inventoryRepository;
	@Mock
	LocationsRepository locationsRepository;
	@Mock
	ProductService productService;
	@Mock
	UserService userService;

	@InjectMocks
	InventoryService inventoryService;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("10");
	private static final double PRODUCT_WEIGHT = 100;


	@BeforeEach
	void setUp() {
		inventoryService = new InventoryService(inventoryRepository, locationsRepository, productService, userService);
	}

	@Test
	public void getInventoryResponse_whenIdIsAvailable_returnsInventoryResponse() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity locationEntity = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,locationEntity,locationEntity.toString());

		when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
		InventoryResponse response = inventoryService.getInventoryResponse(1L);

		assertEquals("TestProduct", response.getProduct());
		assertEquals(20,response.getQuantity());

	}

	@Test
	public void getInventoryResponse_whenIdIsNotAvailable_throwsInventoryNotFoundException() {

		when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(InventoryNotFoundException.class, () -> {
			inventoryService.getInventoryResponse(1L);
		});

	}

	@Test
	public void createStock_whenAllRequirementsAreMet_returnsInventoryResponse() {
		InventoryRequest request = new InventoryRequest(1L,20);
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		when(locationsRepository.count()).thenReturn(5L);
		when(productService.getProductById(1L)).thenReturn(product);

		inventoryService.createStock(authenticatedUser,request);

		verify(locationsRepository).save(any(LocationEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

	}

	@Test
	public void createStock_whenLocationsNotExist_throwsLocationsNotCreatedException() {
		InventoryRequest request = new InventoryRequest(1L,20);
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);

		assertThrows(LocationsNotCreatedException.class,
				() ->inventoryService.createStock(authenticatedUser,request)
		);
		verify(locationsRepository, never()).save(any(LocationEntity.class));
		verify(inventoryRepository, never()).save(any(InventoryEntity.class));

	}

	private ProductEntity createProductHelper(UserEntity user){
		return new ProductEntity(user,PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}

	private LocationEntity createLocationHelper(ProductEntity product) {
		return new LocationEntity(product, LocationType.BLOCK,20,true);
	}

	private InventoryEntity createInventoryHelper(ProductEntity product, LocationEntity locationEntity, String location) {
		return new InventoryEntity(product, locationEntity, 20, location);
	}

	private AuthenticatedUser createAuthenticatedAdminHelper(){

		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser){
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}


}

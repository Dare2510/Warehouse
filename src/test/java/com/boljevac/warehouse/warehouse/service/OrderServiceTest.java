package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.repository.ProductRepository;
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
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private InventoryRepository inventoryRepository;
	@Mock
	private LocationsRepository locationsRepository;
	@Mock
	private ProductService productService;
	@Mock
	private ModelMapper modelMapper;
	@Mock
	private UserService userService;

	@InjectMocks
	private OrderService orderService;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("10");
	private static final double PRODUCT_WEIGHT = 100;

	@BeforeEach
	public void setup() {
		orderService = new OrderService(
				orderRepository,
				inventoryRepository,
				productRepository,
				locationsRepository,
				productService,
				modelMapper,
				userService);
	}

	private LocationEntity createLocationHelper(ProductEntity product) {
		return new LocationEntity(product, LocationType.BLOCK, 20, true);
	}

	private InventoryEntity createInventoryHelper(ProductEntity product, LocationEntity locationEntity, String location) {
		return new InventoryEntity(product, locationEntity, 20, location);
	}

	@Test
	public void createOrder_whenOrderRequestExceedsStock_throwsOrderExceedsStockException() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product, location, location.toString());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		assertThrows(OrderExceedsStockException.class,
				() -> orderService.createOrder(authenticatedUser, new OrderRequest(1L, 30))
		);
		verify(orderRepository, never()).save(any());
	}

	@Test
	public void cancelOrder_whenOrderStatusIsNotValidForCancel_throwsOrderCancelNotPossibleException() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		OrderEntity order = new OrderEntity(product, 30);
		order.setOrderStatus(OrderStatus.PROCESSING);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

		assertThrows(OrderCancelOrDeleteNotPossibleException.class,
				() -> orderService.cancelOrder(authenticatedUser, 1L)
		);
		verify(orderRepository, never()).save(any());
		verify(productRepository, never()).save(any());
		verify(locationsRepository, never()).save(any());
	}

	@Test
	public void cancelOrder_whenStatusIsValidForCancel_returnsOrderResponse() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		OrderEntity order = new OrderEntity(product, 5);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		when(productService.getProductById(order.getProductEntity().getId())).thenReturn(order.getProductEntity());

		orderService.cancelOrder(authenticatedUser, 1L);

		verify(orderRepository).save(order);
		verify(locationsRepository).save(any(LocationEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

		assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());


	}

	@Test
	public void createOrder_whenOrderQuantityIsMoreThanAvailableQuantity_throwsOrderExceedsStockException() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		OrderRequest orderRequest = new OrderRequest(1L, 30);
		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product, location, location.toString());
		inventory.setQuantity(20);

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		assertThrows(OrderExceedsStockException.class,
				() -> orderService.createOrder(authenticatedUser, orderRequest));

		verify(orderRepository, never()).save(any());
		verify(locationsRepository, never()).save(any(LocationEntity.class));
		verify(inventoryRepository, never()).save(any(InventoryEntity.class));

		assertEquals(20, inventory.getQuantity());


	}

	@Test
	public void createOrder_whenAllRequirementsAreMetAndOnlyOneLocationIsNeeded_returnsOrderResponse() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product, location, location.toString());
		OrderRequest request = new OrderRequest(1L, 1);
		OrderEntity order = new OrderEntity(product, request.getQuantity());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		orderService.createOrder(authenticatedUser, request);

		verify(orderRepository).save(any(OrderEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

		assertEquals(1, order.getQuantity());
		assertEquals("TestProduct", order.getProductEntity().getProduct());
		assertEquals(BigDecimal.TEN, order.getTotalPrice());

	}

	@Test
	public void createOrder_whenAllRequirementsAreMetAndTwoLocationsAreNeeded_returnsOrderResponse() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);
		LocationEntity locationA = createLocationHelper(product);
		LocationEntity locationB = createLocationHelper(product);
		InventoryEntity inventoryA = createInventoryHelper(product, locationA, locationA.toString());
		InventoryEntity inventoryB = createInventoryHelper(product, locationB, locationA.toString());
		inventoryA.setQuantity(20);
		inventoryB.setQuantity(20);

		OrderRequest request = new OrderRequest(1L, 30);
		OrderEntity order = new OrderEntity(product, request.getQuantity());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventoryA, inventoryB));

		orderService.createOrder(authenticatedUser, request);

		verify(orderRepository).save(any(OrderEntity.class));
		verify(inventoryRepository, times(2)).save(any(InventoryEntity.class));

		assertEquals(0, inventoryA.getQuantity());
		assertEquals(10, inventoryB.getQuantity());
		assertEquals(0, locationA.getQuantity());
		assertEquals(10, locationB.getQuantity());

		assertEquals(30, order.getQuantity());
		assertEquals("TestProduct", order.getProductEntity().getProduct());
		assertEquals(BigDecimal.valueOf(300), order.getTotalPrice());


	}

	@Test
	public void getOrderById_whenOrderIsAvailable_returnsOrderResponse() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		UserEntity user = getUserByAuthenticatedUser(authenticatedUser);
		ProductEntity product = createProductHelper(user);

		OrderEntity order = new OrderEntity(product, 3);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		orderService.getOrderById(authenticatedUser, 1L);

		verify(orderRepository).findById(1L);
		assertEquals(order, orderService.getOrderById(authenticatedUser, 1L));

	}

	@Test
	public void getOrderById_whenOrderIsNotAvailable_throwsOrderNotFoundException() {
		AuthenticatedUser authenticatedUser = createAuthenticatedAdminHelper();
		assertThrows(OrderNotFoundException.class,
				() -> orderService.getOrderById(authenticatedUser, 1L));

		verify(orderRepository).findById(1L);
	}

	private AuthenticatedUser createAuthenticatedAdminHelper() {
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private UserEntity getUserByAuthenticatedUser(AuthenticatedUser authenticatedUser) {
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}

	private ProductEntity createProductHelper(UserEntity user) {
		return new ProductEntity(user, PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}


}

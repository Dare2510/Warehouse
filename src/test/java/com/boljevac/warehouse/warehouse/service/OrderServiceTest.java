package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	OrderRepository orderRepository;
	@Mock
	ProductRepository productRepository;
	@Mock
	InventoryRepository inventoryRepository;
	@Mock
	LocationsRepository locationsRepository;
	@Mock
	ProductService productService;
	@Mock
	ModelMapper modelMapper;


	@InjectMocks
	OrderService orderService;



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
	public void createOrder_whenOrderRequestExceedsStock_throwsOrderExceedsStockException() {
		ProductEntity product = createProductHelper();
		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,location, location.toString());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		assertThrows(OrderExceedsStockException.class,
				() -> orderService.createOrder(new OrderRequest(1L, 30))
		);
		verify(orderRepository, never()).save(any());
	}

	@Test
	public void cancelOrder_whenOrderStatusIsNotValidForCancel_throwsOrderCancelNotPossibleException() {
		OrderEntity order = new OrderEntity(createProductHelper(), 30);
		order.setOrderStatus(OrderStatus.PROCESSING);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

		assertThrows(OrderCancelOrDeleteNotPossibleException.class,
				() -> orderService.cancelOrder(1L)
		);
		verify(orderRepository, never()).save(any());
		verify(productRepository, never()).save(any());
		verify(locationsRepository, never()).save(any());
	}

	@Test
	public void cancelOrder_whenStatusIsValidForCancel_returnsOrderResponse() {
		OrderEntity order = new OrderEntity(createProductHelper(), 5);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		when(productService.getProductById(order.getProductEntity().getId())).thenReturn(order.getProductEntity());

		orderService.cancelOrder(1L);

		verify(orderRepository).save(order);
		verify(locationsRepository).save(any(LocationEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

		assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());


	}

	@Test
	public void createOrder_whenOrderQuantityIsMoreThanAvailableQuantity_throwsOrderExceedsStockException() {
		OrderRequest orderRequest = new OrderRequest(1L, 30);
		ProductEntity product = createProductHelper();
		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,location, location.toString());
		inventory.setQuantity(20);

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		assertThrows(OrderExceedsStockException.class,
				() -> orderService.createOrder(orderRequest));

		verify(orderRepository, never()).save(any());
		verify(locationsRepository, never()).save(any(LocationEntity.class));
		verify(inventoryRepository, never()).save(any(InventoryEntity.class));

		assertEquals(20,inventory.getQuantity());


	}

	@Test
	public void createOrder_whenAllRequirementsAreMetAndOnlyOneLocationIsNeeded_returnsOrderResponse() {
		ProductEntity product = createProductHelper();
		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,location, location.toString());
		OrderRequest request = new OrderRequest(1L, 1);
		OrderEntity order = new OrderEntity(product,request.getQuantity());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		orderService.createOrder(request);

		verify(orderRepository).save(any(OrderEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

		assertEquals(1, order.getQuantity());
		assertEquals("TestProduct", order.getProductEntity().getProduct());
		assertEquals(BigDecimal.TEN, order.getTotalPrice());

	}
	@Test
	public void createOrder_whenAllRequirementsAreMetAndTwoLocationsAreNeeded_returnsOrderResponse() {
		ProductEntity product = createProductHelper();
		LocationEntity locationA = createLocationHelper(product);
		LocationEntity locationB = createLocationHelper(product);
		InventoryEntity inventoryA = createInventoryHelper(product,locationA, locationA.toString());
		InventoryEntity inventoryB = createInventoryHelper(product,locationB, locationA.toString());
		inventoryA.setQuantity(20);inventoryB.setQuantity(20);

		OrderRequest request = new OrderRequest(1L, 30);
		OrderEntity order = new OrderEntity(product,request.getQuantity());

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventoryA, inventoryB));

		orderService.createOrder(request);

		verify(orderRepository).save(any(OrderEntity.class));
		verify(inventoryRepository, times(2)).save(any(InventoryEntity.class));

		assertEquals(0,inventoryA.getQuantity());
		assertEquals(10,inventoryB.getQuantity());
		assertEquals(0,locationA.getQuantity());
		assertEquals(10,locationB.getQuantity());

		assertEquals(30, order.getQuantity());
		assertEquals("TestProduct", order.getProductEntity().getProduct());
		assertEquals(BigDecimal.valueOf(300), order.getTotalPrice());


	}

	@Test
	public void getOrderById_whenOrderIsAvailable_returnsOrderResponse() {
		OrderEntity order = new OrderEntity(createProductHelper(), 3);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		orderService.getOrderById(1L);

		verify(orderRepository).findById(1L);
		assertEquals(order, orderService.getOrderById(1L));

	}

	@Test
	public void getOrderById_whenOrderIsNotAvailable_throwsOrderNotFoundException() {
		assertThrows(OrderNotFoundException.class,
				() -> orderService.getOrderById(1L));

		verify(orderRepository).findById(1L);
	}


}

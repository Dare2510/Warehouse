package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.warehouse.inventory.repository.InventoryRepository;
import com.boljevac.warehouse.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.warehouse.location.entity.LocationType;
import com.boljevac.warehouse.warehouse.location.repository.LocationsRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import com.boljevac.warehouse.warehouse.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	public void order_exceeds_stock_throws() {
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
	public void order_cancel_not_possible_throws() {
		OrderEntity order = new OrderEntity(createProductHelper(), 30);
		order.setOrderStatus(OrderStatus.PROCESSING);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

		assertThrows(OrderCancelNotPossibleException.class,
				() -> orderService.cancelOrder(1L)
		);
		verify(orderRepository, never()).save(any());
		verify(productRepository, never()).save(any());
		verify(locationsRepository, never()).save(any());
	}

	@Test
	public void order_cancel_success() {
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
	public void test_create_Order_success() {
		ProductEntity product = createProductHelper();
		LocationEntity location = createLocationHelper(product);
		InventoryEntity inventory = createInventoryHelper(product,location, location.toString());
		OrderRequest request = new OrderRequest(1L, 1);

		when(productService.getProductById(1L)).thenReturn(product);
		when(inventoryRepository.getAllByProductEntity(product)).thenReturn(List.of(inventory));

		OrderResponse orderResponse = orderService.createOrder(request);

		verify(orderRepository).save(any(OrderEntity.class));
		verify(inventoryRepository).save(any(InventoryEntity.class));

		assertEquals(1, orderResponse.quantity());
		assertEquals("TestProduct", orderResponse.product());
		assertEquals(10, orderResponse.totalPrice().doubleValue());

	}

	@Test
	public void get_order_by_id() {
		OrderEntity order = new OrderEntity(createProductHelper(), 3);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		orderService.getOrderById(1L);

		verify(orderRepository).findById(1L);
		assertEquals(order, orderService.getOrderById(1L));

	}

	@Test
	public void get_order_by_product_id() {
		assertThrows(OrderNotFoundException.class,
				() -> orderService.getOrderById(1L));

		verify(orderRepository).findById(1L);
	}


}

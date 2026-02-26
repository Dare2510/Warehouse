package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.dto.OrderRequest;
import com.boljevac.warehouse.warehouse.order.dto.OrderResponse;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderExceedsStockException;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.warehouse.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	OrderRepository orderRepository;
	@Mock
	ProductRepository productRepository;

	@InjectMocks
	OrderService orderService;


	public ProductEntity createProductHelper(String product, BigDecimal value, int quantity){
		return new ProductEntity(product,value,quantity);
	}

	@Test
	public void order_exceedsStock_throws() {
		ProductEntity product = createProductHelper(
				"TestProduct", BigDecimal.valueOf(500), 10
		);

		when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));

		assertThrows(OrderExceedsStockException.class,
				() -> orderService.createOrder(new OrderRequest(1L, 30))
				);
		verify(orderRepository, never()).save(any());
	}

	@Test
	public void order_cancel_not_possible_throws() {
		OrderEntity order = new OrderEntity(
				new ProductEntity("TestProduct",
						BigDecimal.valueOf(50),
						100),
				30);

		order.setStatus(OrderStatus.PROCESSING);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

		assertThrows(OrderCancelNotPossibleException.class,
				() -> orderService.cancelOrder(1L)
		);
		verify(orderRepository, never()).save(any());
		verify(productRepository, never()).save(any());
	}

	@Test
	public void order_cancel_successful() {
		ProductEntity product = createProductHelper(
				"TestProduct", BigDecimal.valueOf(500), 10
		);
		OrderEntity order = new OrderEntity(
				product, 5);

		when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));
		when(productRepository.findByProduct("TestProduct")).thenReturn(product);

		OrderResponse response = orderService.cancelOrder(1L);

		assertEquals(OrderStatus.CANCELLED, order.getStatus());
		assertEquals(15, product.getQuantity());

		verify(productRepository).save(product);
		verify(orderRepository).save(order);


	}

	@Test
	public void test_create_Order(){
		Long id = 1L;
		ProductEntity product = createProductHelper(
				"TestProduct", BigDecimal.valueOf(500), 10
		);
		OrderRequest orderRequest = new OrderRequest(
				id,
				1
		);

		when(productRepository.findById(id)).thenReturn(java.util.Optional.of(product));

		OrderResponse orderResponse = orderService.createOrder(orderRequest);
		assertEquals(9,product.getQuantity());

		assertEquals(1,orderResponse.quantity());
		assertEquals("TestProduct",orderResponse.product());
		assertEquals(500,orderResponse.totalPrice().doubleValue());

		verify(productRepository).save(product);



	}





























}

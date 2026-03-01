package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.warehouse.processor.service.ProcessorService;
import com.boljevac.warehouse.warehouse.product.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessorServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ShippedOrdersRepository  shippedOrdersRepository;

	@InjectMocks
	private ProcessorService processorService;

	@Test
	public void change_Order_Status_Success() {
		OrderEntity  orderEntity = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						1000),
				3
		);

		orderEntity.setStatus(OrderStatuses.ORDER_PLACED);

		Long id = 1L;

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

		processorService.changeOrderStatus(id, OrderStatuses.CANCELLED);

		assertEquals(OrderStatuses.CANCELLED, orderEntity.getStatus());

		verify(orderRepository).save(orderEntity);
	}

	@Test
	public void change_Order_Status_Failure_throws() {
		OrderEntity  orderEntity = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						1000),
				3
		);
		Long  id = 1L;
		orderEntity.setStatus(OrderStatuses.ORDER_PLACED);

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

		assertThrows(StatusChangeInvalidOrderException.class, () -> {
			processorService.changeOrderStatus(id, OrderStatuses.SHIPPED);
		});

		assertEquals(OrderStatuses.ORDER_PLACED, orderEntity.getStatus());
		verify(orderRepository, never()).save(any());

	}

	@Test
	public void delete_Order_by_id_Success() {
		Long  id = 1L;
		OrderEntity orderEntity = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						500
				),30);
		orderEntity.setStatus(OrderStatuses.CANCELLED);
		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

			processorService.deleteOrderById(id);

		verify(orderRepository).delete(orderEntity);
		assertFalse(orderRepository.existsById(id));
	}

	@Test
	public void delete_Order_by_id_Failure_throws() {
		Long  id = 1L;
		OrderEntity orderEntity = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						500
				),30);
		orderEntity.setStatus(OrderStatuses.PROCESSING);
		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

		assertThrows(OrderCancelNotPossibleException.class, () -> {
			processorService.deleteOrderById(id);
		});

		verify(orderRepository, never()).deleteById(id);
	}

	@Test
	public void move_shipped_Orders_Success() {
		OrderEntity a = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500),30);

		OrderEntity b = new OrderEntity(
				new ProductEntity(
						"TestProductB",
						BigDecimal.valueOf(50),
						600),10);

		a.setStatus(OrderStatuses.SHIPPED);
		b.setStatus(OrderStatuses.SHIPPED);

		List<OrderEntity> shippedOrders = List.of(a,b);
		when(orderRepository.getOrdersByStatus(OrderStatuses.SHIPPED)).thenReturn(shippedOrders);

		processorService.moveShippedOrders();

		verify(orderRepository).deleteAll(shippedOrders);

		ArgumentCaptor<List<ShippedEntity>> orderEntityArgumentCaptor = ArgumentCaptor.forClass((Class)List.class);
		verify(shippedOrdersRepository).saveAll(orderEntityArgumentCaptor.capture());

		List<ShippedEntity> saved = orderEntityArgumentCaptor.getValue();
		assertEquals(2, saved.size());


	}
}

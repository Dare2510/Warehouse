package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessorServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ShippedOrdersRepository shippedOrdersRepository;

	@InjectMocks
	private ProcessorService processorService;

	@Test
	public void change_Order_Status_Success() {
		OrderEntity orderWithValidStatus = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						1000),
				3
		);

		orderWithValidStatus.setStatus(OrderStatuses.ORDER_PLACED);

		Long id = 1L;

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderWithValidStatus));

		processorService.changeOrderStatus(id, OrderStatuses.CANCELLED);

		assertEquals(OrderStatuses.CANCELLED, orderWithValidStatus.getStatus());

		verify(orderRepository).save(orderWithValidStatus);
	}

	@Test
	public void change_Order_Status_Failure_throws() {
		OrderEntity orderWithInvalidStatus = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						1000),
				3
		);
		Long id = 1L;
		orderWithInvalidStatus.setStatus(OrderStatuses.ORDER_PLACED);

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderWithInvalidStatus));

		assertThrows(StatusChangeInvalidOrderException.class, () -> {
			processorService.changeOrderStatus(id, OrderStatuses.SHIPPED);
		});

		assertEquals(OrderStatuses.ORDER_PLACED, orderWithInvalidStatus.getStatus());
		verify(orderRepository, never()).save(any());

	}

	@Test
	public void delete_Order_by_id_Success() {
		Long id = 1L;
		OrderEntity cancelledOrder = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						500
				), 30);
		cancelledOrder.setStatus(OrderStatuses.CANCELLED);
		when(orderRepository.findById(id)).thenReturn(Optional.of(cancelledOrder));

		processorService.deleteOrderById(id);

		verify(orderRepository).delete(cancelledOrder);
		assertFalse(orderRepository.existsById(id));
	}

	@Test
	public void delete_Order_by_id_Failure_throws() {
		Long id = 1L;
		OrderEntity processingOrder = new OrderEntity(
				new ProductEntity(
						"TestProduct",
						BigDecimal.valueOf(30),
						500
				), 30);
		processingOrder.setStatus(OrderStatuses.PROCESSING);
		when(orderRepository.findById(id)).thenReturn(Optional.of(processingOrder));

		assertThrows(OrderCancelNotPossibleException.class, () -> {
			processorService.deleteOrderById(id);
		});

		verify(orderRepository, never()).deleteById(id);
	}

	@Test
	public void move_shipped_Orders_Success() {
		OrderEntity shippedOrderA = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);

		OrderEntity shippedOrderB = new OrderEntity(
				new ProductEntity(
						"TestProductB",
						BigDecimal.valueOf(50),
						600), 10);

		shippedOrderA.setStatus(OrderStatuses.SHIPPED);
		shippedOrderB.setStatus(OrderStatuses.SHIPPED);

		List<OrderEntity> shippedOrders = List.of(shippedOrderA, shippedOrderB);
		when(orderRepository.getOrdersByStatus(OrderStatuses.SHIPPED)).thenReturn(shippedOrders);

		processorService.moveShippedOrders();

		verify(orderRepository).deleteAll(shippedOrders);

		ArgumentCaptor<List<ShippedEntity>> orderEntityArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
		verify(shippedOrdersRepository).saveAll(orderEntityArgumentCaptor.capture());

		List<ShippedEntity> saved = orderEntityArgumentCaptor.getValue();
		assertEquals(2, saved.size());

	}

	@Test
	public void move_shipped_Orders_throws() {
		OrderEntity cancelledOrder = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);

		OrderEntity processingOrder = new OrderEntity(
				new ProductEntity(
						"TestProductB",
						BigDecimal.valueOf(50),
						600), 10);

		cancelledOrder.setStatus(OrderStatuses.CANCELLED);
		processingOrder.setStatus(OrderStatuses.PROCESSING);

		List<OrderEntity> orders = List.of(cancelledOrder, processingOrder);
		when(orderRepository.getOrdersByStatus(OrderStatuses.SHIPPED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.moveShippedOrders();
		});

		verify(orderRepository, never()).deleteAll(anyList());
		verify(orderRepository, never()).saveAll(orders);

	}

	@Test
	public void delete_cancelled_Order_by_id_success() {
		OrderEntity cancelledOrder = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);
		cancelledOrder.setStatus(OrderStatuses.CANCELLED);
		Long id = 1L;
		when(orderRepository.findById(id)).thenReturn(Optional.of(cancelledOrder));
		processorService.deleteOrderById(id);

		verify(orderRepository).delete(cancelledOrder);

	}

	@Test
	public void delete_cancelled_Order_by_id_throws() {
		OrderEntity processingOrder = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);
		processingOrder.setStatus(OrderStatuses.PROCESSING);
		Long id = 1L;
		when(orderRepository.findById(id)).thenReturn(Optional.empty());
		assertThrows(OrderNotFoundException.class, () -> {
			processorService.deleteOrderById(id);
		});
		verify(orderRepository, never()).delete(processingOrder);
	}

	@Test
	public void delete_all_cancelled_Orders_success() {
		OrderEntity cancelledOrderA = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);

		OrderEntity cancelledOrderB = new OrderEntity(
				new ProductEntity(
						"TestProductB",
						BigDecimal.valueOf(50),
						600), 10);

		cancelledOrderA.setStatus(OrderStatuses.CANCELLED);
		cancelledOrderB.setStatus(OrderStatuses.CANCELLED);

		List<OrderEntity> cancelledOrders = List.of(cancelledOrderA, cancelledOrderB);

		when(orderRepository.getOrdersByStatus(OrderStatuses.CANCELLED)).thenReturn(cancelledOrders);
		processorService.deleteCancelledOrders();
		verify(orderRepository).deleteAll(cancelledOrders);
	}

	@Test
	public void delete_all_cancelled_Orders_throws() {
		OrderEntity processingOrder = new OrderEntity(
				new ProductEntity(
						"TestProductA",
						BigDecimal.valueOf(30),
						500), 30);

		OrderEntity shippedOrder = new OrderEntity(
				new ProductEntity(
						"TestProductB",
						BigDecimal.valueOf(50),
						600), 10);
		processingOrder.setStatus(OrderStatuses.PROCESSING);
		processingOrder.setStatus(OrderStatuses.SHIPPED);

		List<OrderEntity> orders = List.of(processingOrder, shippedOrder);

		when(orderRepository.getOrdersByStatus(OrderStatuses.CANCELLED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.deleteCancelledOrders();
		});
		verify(orderRepository, never()).deleteAll(orders);
	}
}

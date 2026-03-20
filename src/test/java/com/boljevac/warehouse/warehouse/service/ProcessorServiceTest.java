package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
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

	@Mock
	private OrderService orderService;

	private ProductEntity createProductHelper() {
		return new ProductEntity("TestProduct", BigDecimal.TEN, 100);
	}

	@Test
	public void changeStatusOfOrder_whenRequestedStatusIsValid_returnsProcessorResponse() {
		OrderEntity orderWithValidStatus = new OrderEntity(createProductHelper(), 3);
		orderWithValidStatus.setOrderStatus(OrderStatus.ORDER_PLACED);

		when(orderService.getOrderById(1L)).thenReturn(orderWithValidStatus);
		processorService.changeStatusOfOrder(1L, OrderStatus.PROCESSING);

		assertEquals(OrderStatus.PROCESSING, orderWithValidStatus.getOrderStatus());

		verify(orderRepository).save(orderWithValidStatus);
	}

	@Test
	public void changeStatusOfOrder_whenRequestedStatusIsNotValid_throwsStatusChangeInvalidOrderException() {
		OrderEntity orderWithInvalidStatus = new OrderEntity(createProductHelper(), 3);
		orderWithInvalidStatus.setOrderStatus(OrderStatus.ORDER_PLACED);

		when(orderService.getOrderById(1L)).thenReturn(orderWithInvalidStatus);

		assertThrows(StatusChangeInvalidOrderException.class, () -> {
			processorService.changeStatusOfOrder(1L, OrderStatus.SHIPPED);
		});

		assertEquals(OrderStatus.ORDER_PLACED, orderWithInvalidStatus.getOrderStatus());

		verify(orderRepository, never()).save(any());

	}

	@Test
	public void deleteOrderById_whenOrderStatusIsCancelled_returnsProcessorResponse() {
		OrderEntity cancelledOrder = new OrderEntity(createProductHelper(), 30);
		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

		when(orderService.getOrderById(1L)).thenReturn(cancelledOrder);
		processorService.deleteOrderById(1L);

		verify(orderRepository).delete(cancelledOrder);
		assertFalse(orderRepository.existsById(1L));
	}

	@Test
	public void deleteOrderById_whenOrderStatusIsNotQualifiedForDeletion_throwsOrderCancelOrDeleteNotPossibleException() {
		OrderEntity processingOrder = new OrderEntity(createProductHelper(), 30);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		when(orderService.getOrderById(1L)).thenReturn(processingOrder);

		assertThrows(OrderCancelOrDeleteNotPossibleException.class, () -> {
			processorService.deleteOrderById(1L);
		});
		verify(orderRepository, never()).deleteById(1L);
	}

	@Test
	public void archiveShippedOrders_whenOrdersWithStatusShippedAvailable_returnsProcessorResponse() {
		OrderEntity shippedOrderA = new OrderEntity(createProductHelper(), 30);
		OrderEntity shippedOrderB = new OrderEntity(createProductHelper(), 10);

		shippedOrderA.setOrderStatus(OrderStatus.SHIPPED);
		shippedOrderB.setOrderStatus(OrderStatus.SHIPPED);

		List<OrderEntity> shippedOrders = List.of(shippedOrderA, shippedOrderB);
		when(orderRepository.getByOrderStatus(OrderStatus.SHIPPED)).thenReturn(shippedOrders);

		processorService.archiveShippedOrders();

		verify(orderRepository).deleteAll(shippedOrders);

		ArgumentCaptor<List<ShippedEntity>> orderEntityArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
		verify(shippedOrdersRepository).saveAll(orderEntityArgumentCaptor.capture());

		List<ShippedEntity> saved = orderEntityArgumentCaptor.getValue();
		assertEquals(2, saved.size());

	}

	@Test
	public void archiveShippedOrders_whenNoOrdersWithStatusShipped_throwsOrderNotFoundException() {
		OrderEntity cancelledOrder = new OrderEntity(createProductHelper(), 30);

		OrderEntity processingOrder = new OrderEntity(createProductHelper(), 10);

		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		List<OrderEntity> orders = List.of(cancelledOrder, processingOrder);
		when(orderRepository.getByOrderStatus(OrderStatus.SHIPPED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.archiveShippedOrders();
		});

		verify(orderRepository, never()).deleteAll(anyList());
		verify(orderRepository, never()).saveAll(orders);

	}

	@Test
	public void deleteOrdersById_whenOrderWithStatusCancelledFound_returnsProcessorResponse() {
		OrderEntity cancelledOrder = new OrderEntity(createProductHelper(), 30);
		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

		when(orderService.getOrderById(1L)).thenReturn(cancelledOrder);

		processorService.deleteOrderById(1L);

		verify(orderRepository).delete(cancelledOrder);

	}

	@Test
	public void deleteOrderById_whenTheOrderHasNotStatusCancelled_throwsOrderCancelOrDeleteNotPossibleException() {
		OrderEntity processingOrder = new OrderEntity(createProductHelper(), 30);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		when(orderService.getOrderById(1L)).thenReturn(processingOrder);

		assertThrows(OrderCancelOrDeleteNotPossibleException.class, () -> {
			processorService.deleteOrderById(1L);
		});

		verify(orderRepository, never()).delete(processingOrder);
	}

	@Test
	public void deleteAllCancelledOrders_whenOrdersWithStatusCancelledAvailable_returnsProcessorResponse() {
		OrderEntity cancelledOrderA = new OrderEntity(createProductHelper(), 30);
		OrderEntity cancelledOrderB = new OrderEntity(createProductHelper(), 10);

		cancelledOrderA.setOrderStatus(OrderStatus.CANCELLED);
		cancelledOrderB.setOrderStatus(OrderStatus.CANCELLED);

		List<OrderEntity> cancelledOrders = List.of(cancelledOrderA, cancelledOrderB);

		when(orderRepository.getByOrderStatus(OrderStatus.CANCELLED)).thenReturn(cancelledOrders);
		processorService.deleteAllCancelledOrders();
		verify(orderRepository).deleteAll(cancelledOrders);
	}

	@Test
	public void deleteAllCancelledOrders_whenNoOrdersWithStatusCancelledAvailable_throwsOrderNotFoundException() {
		OrderEntity processingOrder = new OrderEntity(createProductHelper(), 30);
		OrderEntity shippedOrder = new OrderEntity(createProductHelper(), 10);

		processingOrder.setOrderStatus(OrderStatus.PROCESSING);
		processingOrder.setOrderStatus(OrderStatus.SHIPPED);

		List<OrderEntity> orders = List.of(processingOrder, shippedOrder);

		when(orderRepository.getByOrderStatus(OrderStatus.CANCELLED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.deleteAllCancelledOrders();
		});
		verify(orderRepository, never()).deleteAll(orders);
	}
}

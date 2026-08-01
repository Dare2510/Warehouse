package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.processor.service.ProcessorService;
import com.boljevac.warehouse.product.entity.ProductEntity;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.entity.UserEntity;
import com.boljevac.warehouse.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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

	@Mock
	private OrderService orderService;

	@Mock
	private UserService  userService;

	@Mock
	ModelMapper modelMapper;

	@InjectMocks
	private ProcessorService processorService;

	private static final String PRODUCT_NAME = "TestProduct";
	private static final BigDecimal PRODUCT_VALUE = new BigDecimal("10");
	private static final double PRODUCT_WEIGHT = 100;


	@BeforeEach
	public void setup() {
		processorService = new ProcessorService(
				orderRepository,shippedOrdersRepository,orderService,modelMapper,userService);
	}

	@Test
	public void changeStatusOfOrder_whenRequestedStatusIsValid_returnsProcessorResponse() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity orderWithValidStatus = new OrderEntity(product, 3);
		orderWithValidStatus.setOrderStatus(OrderStatus.ORDER_PLACED);

		when(orderService.getOrderById(1L)).thenReturn(orderWithValidStatus);
		processorService.changeStatusOfOrder(authenticatedUser,1L, OrderStatus.PROCESSING);

		assertEquals(OrderStatus.PROCESSING, orderWithValidStatus.getOrderStatus());

		verify(orderRepository).save(orderWithValidStatus);
	}

	@Test
	public void changeStatusOfOrder_whenRequestedStatusIsNotValid_throwsStatusChangeInvalidOrderException() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity orderWithInvalidStatus = new OrderEntity(product, 3);
		orderWithInvalidStatus.setOrderStatus(OrderStatus.ORDER_PLACED);

		when(orderService.getOrderById(1L)).thenReturn(orderWithInvalidStatus);

		assertThrows(StatusChangeInvalidOrderException.class, () -> {
			processorService.changeStatusOfOrder(authenticatedUser,1L, OrderStatus.SHIPPED);
		});

		assertEquals(OrderStatus.ORDER_PLACED, orderWithInvalidStatus.getOrderStatus());

		verify(orderRepository, never()).save(any());

	}

	@Test
	public void deleteOrderById_whenOrderStatusIsCancelled_returnsProcessorResponse() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity cancelledOrder = new OrderEntity(product, 30);
		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

		when(orderService.getOrderById(1L)).thenReturn(cancelledOrder);
		processorService.deleteOrderById(1L);

		verify(orderRepository).delete(cancelledOrder);
		assertFalse(orderRepository.existsById(1L));
	}

	@Test
	public void deleteOrderById_whenOrderStatusIsNotQualifiedForDeletion_throwsOrderCancelOrDeleteNotPossibleException() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity processingOrder = new OrderEntity(product, 30);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		when(orderService.getOrderById(1L)).thenReturn(processingOrder);

		assertThrows(OrderCancelOrDeleteNotPossibleException.class, () -> {
			processorService.deleteOrderById(1L);
		});
		verify(orderRepository, never()).deleteById(1L);
	}

	@Test
	public void archiveShippedOrders_whenOrdersWithStatusShippedAvailable_returnsProcessorResponse() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity shippedOrderA = new OrderEntity(product, 30);
		OrderEntity shippedOrderB = new OrderEntity(product, 10);

		shippedOrderA.setOrderStatus(OrderStatus.SHIPPED);
		shippedOrderB.setOrderStatus(OrderStatus.SHIPPED);

		List<OrderEntity> shippedOrders = List.of(shippedOrderA, shippedOrderB);
		when(orderRepository.getByOrderStatus(OrderStatus.SHIPPED)).thenReturn(shippedOrders);

		processorService.archiveShippedOrders(authenticatedUser);

		verify(orderRepository).deleteAll(shippedOrders);

		ArgumentCaptor<List<ShippedEntity>> orderEntityArgumentCaptor = ArgumentCaptor.forClass((Class) List.class);
		verify(shippedOrdersRepository).saveAll(orderEntityArgumentCaptor.capture());

		List<ShippedEntity> saved = orderEntityArgumentCaptor.getValue();
		assertEquals(2, saved.size());

	}

	@Test
	public void archiveShippedOrders_whenNoOrdersWithStatusShipped_throwsOrderNotFoundException() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);

		OrderEntity cancelledOrder = new OrderEntity(product, 30);

		OrderEntity processingOrder = new OrderEntity(product, 10);

		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		List<OrderEntity> orders = List.of(cancelledOrder, processingOrder);
		when(orderRepository.getByOrderStatus(OrderStatus.SHIPPED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.archiveShippedOrders(authenticatedUser);
		});

		verify(orderRepository, never()).deleteAll(anyList());
		verify(orderRepository, never()).saveAll(orders);

	}

	@Test
	public void deleteOrdersById_whenOrderWithStatusCancelledFound_returnsProcessorResponse() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);
		OrderEntity cancelledOrder = new OrderEntity(product, 30);
		cancelledOrder.setOrderStatus(OrderStatus.CANCELLED);

		when(orderService.getOrderById(1L)).thenReturn(cancelledOrder);

		processorService.deleteOrderById(1L);

		verify(orderRepository).delete(cancelledOrder);

	}

	@Test
	public void deleteOrderById_whenTheOrderHasNotStatusCancelled_throwsOrderCancelOrDeleteNotPossibleException() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);

		OrderEntity processingOrder = new OrderEntity(product, 30);
		processingOrder.setOrderStatus(OrderStatus.PROCESSING);

		when(orderService.getOrderById(1L)).thenReturn(processingOrder);

		assertThrows(OrderCancelOrDeleteNotPossibleException.class, () -> {
			processorService.deleteOrderById(1L);
		});

		verify(orderRepository, never()).delete(processingOrder);
	}

	@Test
	public void deleteAllCancelledOrders_whenOrdersWithStatusCancelledAvailable_returnsProcessorResponse() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);

		OrderEntity cancelledOrderA = new OrderEntity(product, 30);
		OrderEntity cancelledOrderB = new OrderEntity(product, 10);

		cancelledOrderA.setOrderStatus(OrderStatus.CANCELLED);
		cancelledOrderB.setOrderStatus(OrderStatus.CANCELLED);

		List<OrderEntity> cancelledOrders = List.of(cancelledOrderA, cancelledOrderB);

		when(orderRepository.getByOrderStatus(OrderStatus.CANCELLED)).thenReturn(cancelledOrders);
		processorService.deleteAllCancelledOrders();
		verify(orderRepository).deleteAll(cancelledOrders);
	}

	@Test
	public void deleteAllCancelledOrders_whenNoOrdersWithStatusCancelledAvailable_throwsOrderNotFoundException() {
		AuthenticatedUser authenticatedUser = authenticatedAdmin();
		UserEntity user = user(authenticatedUser);
		ProductEntity product = product(user);

		OrderEntity processingOrder = new OrderEntity(product, 30);
		OrderEntity shippedOrder = new OrderEntity(product, 10);

		processingOrder.setOrderStatus(OrderStatus.PROCESSING);
		processingOrder.setOrderStatus(OrderStatus.SHIPPED);

		List<OrderEntity> orders = List.of(processingOrder, shippedOrder);

		when(orderRepository.getByOrderStatus(OrderStatus.CANCELLED)).thenReturn(Collections.emptyList());

		assertThrows(OrderNotFoundException.class, () -> {
			processorService.deleteAllCancelledOrders();
		});
		verify(orderRepository, never()).deleteAll(orders);
	}

	private AuthenticatedUser authenticatedAdmin(){
		return new AuthenticatedUser(99L, "Admin@gmail.com", Role.ADMIN);
	}

	private UserEntity user(AuthenticatedUser authenticatedUser){
		return userService.getUserByAuthenticatedUser(authenticatedUser);
	}

	private ProductEntity product(UserEntity user){
		return new ProductEntity(user,PRODUCT_NAME, PRODUCT_VALUE, PRODUCT_WEIGHT);
	}
}

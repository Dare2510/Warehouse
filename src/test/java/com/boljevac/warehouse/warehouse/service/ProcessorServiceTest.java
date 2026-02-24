package com.boljevac.warehouse.warehouse.service;

import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.processor.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessorServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private ProcessorService processorService;

	@Test
	public void change_Order_Status_Success() {
		OrderEntity  orderEntity = new OrderEntity(
				"TestProduct",
				1L,
				3,
				BigDecimal.valueOf(500)
		);

		orderEntity.setStatus(OrderStatus.ORDER_PLACED);

		Long id = 1L;

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

		processorService.changeOrderStatus(id, OrderStatus.PROCESSING);

		assertEquals(OrderStatus.PROCESSING, orderEntity.getStatus());

		verify(orderRepository).save(orderEntity);
	}

	@Test
	public void change_Order_Status_Failure_throws() {
		OrderEntity  orderEntity = new OrderEntity(
				"TestProduct",
				1L,
				3,
				BigDecimal.valueOf(500)
		);
		Long  id = 1L;
		orderEntity.setStatus(OrderStatus.ORDER_PLACED);

		when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));

		assertThrows(StatusChangeInvalidOrderException.class, () -> {
			processorService.changeOrderStatus(id, OrderStatus.SHIPPED);
		});

		assertEquals(OrderStatus.ORDER_PLACED, orderEntity.getStatus());
		verify(orderRepository, never()).save(any());


	}
}

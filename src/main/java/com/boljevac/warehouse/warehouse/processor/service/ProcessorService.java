package com.boljevac.warehouse.warehouse.processor.service;

import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.warehouse.order.service.OrderService;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.warehouse.processor.dto.ProcessorResponse;
import com.boljevac.warehouse.warehouse.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessorService {

	public final OrderRepository orderRepository;
	public final ShippedOrdersRepository shippedOrdersRepository;
	public final OrderService orderService;

	public ProcessorService(OrderRepository orderRepository, ShippedOrdersRepository shippedOrdersRepository, OrderService orderService) {
		this.orderRepository = orderRepository;
		this.shippedOrdersRepository = shippedOrdersRepository;
		this.orderService = orderService;
	}

	public List<ProcessorResponse> getListOfOrdersByStatus(ProcessorRequest processorRequest) {
		OrderStatus orderStatus = processorRequest.getOrderStatus();

		List<OrderEntity> orderEntityList = orderRepository.getByOrderStatus(orderStatus).stream().toList();

		if (orderEntityList.isEmpty()) {
			throw new OrderNotFoundException();
		}

		List<ProcessorResponse> processorResponses = new ArrayList<>();
		for (OrderEntity orderEntity : orderEntityList) {
			processorResponses.add(new ProcessorResponse(
					orderEntity.getProductEntity().getId(),
					orderEntity.getProductEntity().getProduct(),
					orderEntity.getQuantity(),
					orderEntity.getOrderStatus()
			));
		}

		return processorResponses;

	}

	public ProcessorResponse changeStatusOfOrder(Long orderId, OrderStatus newOrderStatus) {

		OrderEntity orderToChangeStatus = orderService.getOrderById(orderId);
		OrderStatus statusToChange = orderToChangeStatus.getOrderStatus();

		//Validation if the required sequence of statusToChange changes is met
		boolean validStatusChange = statusToChange.validatorCorrectStatusChange(orderToChangeStatus, newOrderStatus);

		if(!validStatusChange) {
			throw new StatusChangeInvalidOrderException();
		}
		orderToChangeStatus.setOrderStatus(newOrderStatus);
		orderRepository.save(orderToChangeStatus);

		return new ProcessorResponse(
				orderToChangeStatus.getProductEntity().getId(),
				orderToChangeStatus.getProductEntity().getProduct(),
				orderToChangeStatus.getQuantity(),
				orderToChangeStatus.getOrderStatus()
		);
	}

	@Transactional
	public void deleteOrderById(Long orderId) {
		OrderEntity orderToDelete = orderService.getOrderById(orderId);
		OrderStatus currentStatus = orderToDelete.getOrderStatus();

		if (currentStatus.equals(OrderStatus.CANCELLED)) {
			orderRepository.delete(orderToDelete);
		} else {
			throw new OrderCancelNotPossibleException(orderId);
		}
	}

	@Transactional
	public void archiveShippedOrders() {

		List<OrderEntity> listOfShippedOrders = orderRepository.getByOrderStatus(OrderStatus.SHIPPED);

		if (listOfShippedOrders.isEmpty()) {
			throw new OrderNotFoundException();
		}
		List<ShippedEntity> shippedEntities = new ArrayList<>();
		for (OrderEntity orderEntity : listOfShippedOrders) {
			shippedEntities.add(new ShippedEntity(
					orderEntity
			));

		}

		shippedOrdersRepository.saveAll(shippedEntities);
		orderRepository.deleteAll(listOfShippedOrders);

	}

	@Transactional
	public void deleteAllCancelledOrders() {
		List<OrderEntity> listOfCancelledOrders = orderRepository.getByOrderStatus(OrderStatus.CANCELLED);

		if (listOfCancelledOrders.isEmpty()) {
			throw new OrderNotFoundException();
		}
		orderRepository.deleteAll(listOfCancelledOrders);

	}
}

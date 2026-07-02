package com.boljevac.warehouse.processor.service;

import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.order.entity.ShippedEntity;
import com.boljevac.warehouse.order.exception.OrderCancelOrDeleteNotPossibleException;
import com.boljevac.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.order.repository.OrderRepository;
import com.boljevac.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.order.service.OrderService;
import com.boljevac.warehouse.processor.dto.ProcessorRequest;
import com.boljevac.warehouse.processor.dto.ProcessorResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProcessorService {

	public final OrderRepository orderRepository;
	public final ShippedOrdersRepository shippedOrdersRepository;
	public final OrderService orderService;
	private final ModelMapper modelMapper;

	public ProcessorService(OrderRepository orderRepository, ShippedOrdersRepository shippedOrdersRepository, OrderService orderService, ModelMapper modelMapper) {
		this.orderRepository = orderRepository;
		this.shippedOrdersRepository = shippedOrdersRepository;
		this.orderService = orderService;
		this.modelMapper = modelMapper;
	}

	public List<ProcessorResponse> getListOfOrdersByStatus(ProcessorRequest processorRequest) {
		OrderStatus orderStatus = processorRequest.getOrderStatus();

		List<OrderEntity> listOfOrdersByStatus = orderRepository.getByOrderStatus(orderStatus).stream().toList();
		boolean ordersExists = validateOrdersExist(listOfOrdersByStatus);

		if (!ordersExists) {
			throw new OrderNotFoundException();
		}

		return listOfOrdersByStatus.stream().filter(
				status -> status.getOrderStatus().equals(orderStatus)).map(
				status -> modelMapper.map(status, ProcessorResponse.class)).toList();
	}


	public ProcessorResponse changeStatusOfOrder(Long orderId, OrderStatus newOrderStatus) {

		OrderEntity orderToChangeStatus = orderService.getOrderById(orderId);
		OrderStatus statusToChange = orderToChangeStatus.getOrderStatus();

		//Validation if the required sequence of statusToChange changes is met
		boolean validStatusChange = statusToChange.validatorCorrectStatusChange(orderToChangeStatus, newOrderStatus);

		if (!validStatusChange) {
			throw new StatusChangeInvalidOrderException();
		}
		orderToChangeStatus.setOrderStatus(newOrderStatus);
		orderRepository.save(orderToChangeStatus);
		log.info("Order with Id {} has been changed", orderToChangeStatus.getId());

		return modelMapper.map(orderToChangeStatus, ProcessorResponse.class);

//		return new ProcessorResponse(
//				orderToChangeStatus.getProductEntity().getId(),
//				orderToChangeStatus.getProductEntity().getProduct(),
//				orderToChangeStatus.getQuantity(),
//				orderToChangeStatus.getOrderStatus()
//		);
	}

	@Transactional
	public void deleteOrderById(Long orderId) {
		OrderEntity orderToDelete = orderService.getOrderById(orderId);
		boolean deletionIsValid = validateToDeleteOrder(orderToDelete);

		if (deletionIsValid) {
			log.info("Order with Id {} has been deleted", orderToDelete.getId());
			orderRepository.delete(orderToDelete);
		} else {
			throw new OrderCancelOrDeleteNotPossibleException(orderId);
		}
	}

	@Transactional
	public void archiveShippedOrders() {

		List<OrderEntity> listOfShippedOrders = orderRepository.getByOrderStatus(OrderStatus.SHIPPED);

		boolean shippedOrdersExists = validateOrdersExist(listOfShippedOrders);

		if (!shippedOrdersExists) {
			throw new OrderNotFoundException();
		}

		List<ShippedEntity> shippedEntities = new ArrayList<>();

		for (OrderEntity orderEntity : listOfShippedOrders) {
			shippedEntities.add(new ShippedEntity(
					orderEntity
			));

		}
		log.info("Shipped orders have been archived");
		shippedOrdersRepository.saveAll(shippedEntities);
		orderRepository.deleteAll(listOfShippedOrders);

	}

	@Transactional
	public void deleteAllCancelledOrders() {
		List<OrderEntity> listOfCancelledOrders = orderRepository.getByOrderStatus(OrderStatus.CANCELLED);

		boolean ordersToCancelExists = validateOrdersExist(listOfCancelledOrders);

		if (!ordersToCancelExists) {
			throw new OrderNotFoundException();
		}
		log.info("Cancelled orders have been deleted");
		orderRepository.deleteAll(listOfCancelledOrders);


	}

	private boolean validateOrdersExist(List<OrderEntity> listOfOrders) {
		return !listOfOrders.isEmpty();

	}

	public boolean validateToDeleteOrder(OrderEntity orderToDelete) {
		return orderToDelete.getOrderStatus().equals(OrderStatus.CANCELLED);

	}
}

package com.boljevac.warehouse.warehouse.processor.service;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
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

	public ProcessorService(OrderRepository orderRepository, ShippedOrdersRepository shippedOrdersRepository) {
		this.orderRepository = orderRepository;
		this.shippedOrdersRepository = shippedOrdersRepository;
	}

	//Helper Method to get Order
	public OrderEntity getOrderById(Long id) throws OrderNotFoundException {
		return orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);
	}

	public List<ProcessorResponse> getOrders(ProcessorRequest processorRequest) {
		OrderStatuses orderStatuses = processorRequest.getOrderStatus();

		List<OrderEntity> orderEntityList = orderRepository
				.getOrdersByStatus(orderStatuses).stream().toList();
		if(orderEntityList.isEmpty()) {
			throw new OrderNotFoundException();
		}

		List<ProcessorResponse> processorResponses = new ArrayList<>();
		for(OrderEntity orderEntity : orderEntityList) {
			processorResponses.add(new ProcessorResponse(
					orderEntity.getProductEntity().getId(),
					orderEntity.getProductEntity().getProduct(),
					orderEntity.getQuantity(),
					orderEntity.getStatus()
			));
		}

		return processorResponses;

	}

	public ProcessorResponse changeOrderStatus(Long id, OrderStatuses orderStatus) {
		OrderEntity toChange = getOrderById(id);

		OrderStatuses status = toChange.getStatus();

		status.sequenceValidator(toChange,orderStatus);


			toChange.setStatus(orderStatus);
			orderRepository.save(toChange);

			return new ProcessorResponse(
					toChange.getProductEntity().getId(),
					toChange.getProductEntity().getProduct(),
					toChange.getQuantity(),
					toChange.getStatus()
			);


		}

	@Transactional
	public void deleteOrderById(Long id) {
		OrderEntity toDelete = getOrderById(id);

		if(toDelete.getStatus().equals(OrderStatuses.CANCELLED)) {
			orderRepository.delete(toDelete);
		} else{
			throw new OrderCancelNotPossibleException(id);
		}
	}
	@Transactional
	public void moveShippedOrders() {

		List<OrderEntity> shippedOrders = orderRepository.
				getOrdersByStatus(OrderStatuses.SHIPPED);

		if(shippedOrders.isEmpty()) {
			throw new OrderNotFoundException();
		}
		List<ShippedEntity> shippedEntities = new ArrayList<>();
		for(OrderEntity orderEntity : shippedOrders) {
			shippedEntities.add(new ShippedEntity(
					orderEntity
			));

		}

		shippedOrdersRepository.saveAll(shippedEntities);
		orderRepository.deleteAll(shippedOrders);

	}
	@Transactional
	public void deleteCancelledOrders(){
			List<OrderEntity> shippedOrders = orderRepository.
					getOrdersByStatus(OrderStatuses.CANCELLED);

			if(shippedOrders.isEmpty()) {
				throw new OrderNotFoundException();
			}
			orderRepository.deleteAll(shippedOrders);

	}
}

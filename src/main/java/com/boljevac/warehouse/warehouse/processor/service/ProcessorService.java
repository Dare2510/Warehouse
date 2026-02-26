package com.boljevac.warehouse.warehouse.processor.service;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;
import com.boljevac.warehouse.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import com.boljevac.warehouse.warehouse.order.repository.ShippedOrdersRepository;
import com.boljevac.warehouse.warehouse.order.exception.OrderCancelNotPossibleException;
import com.boljevac.warehouse.warehouse.order.exception.OrderNotFoundException;
import com.boljevac.warehouse.warehouse.order.entity.ShippedEntity;
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



	public List<ProcessorResponse> getOrders(ProcessorRequest processorRequest) {
		OrderStatus orderStatus = processorRequest.getOrderStatus();

		List<OrderEntity> orderEntityList = orderRepository
				.getOrdersByStatus(orderStatus).stream().toList();
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

	public ProcessorResponse changeOrderStatus(Long id, OrderStatus status) {
		OrderEntity toChange = orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);

		switch(toChange.getStatus()) {
			case ORDER_PLACED:
				if(status != OrderStatus.PROCESSING) {
					throw new StatusChangeInvalidOrderException();
				} else {
					toChange.setStatus(status);
				}
				break;
			case PROCESSING:
				if(status != OrderStatus.PACKAGED) {
					throw new StatusChangeInvalidOrderException();
				} else {
					toChange.setStatus(status);
				}
				break;
			case PACKAGED:
				if(status != OrderStatus.SHIPPED) {
					throw new StatusChangeInvalidOrderException();
				} else {
					toChange.setStatus(status);
				}
				break;
			default:
				throw new StatusChangeInvalidOrderException();
		}
			toChange.setStatus(status);
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
		OrderEntity toDelete = orderRepository.findById(id).orElseThrow(
				OrderNotFoundException::new
		);

		if(toDelete.getStatus().equals(OrderStatus.CANCELLED)) {
			orderRepository.delete(toDelete);
		} else{
			throw new OrderCancelNotPossibleException(id);
		}
	}
	@Transactional
	public void moveShippedOrders() {

		List<OrderEntity> shippedOrders = orderRepository.
				getOrdersByStatus(OrderStatus.SHIPPED);

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
					getOrdersByStatus(OrderStatus.CANCELLED);

			if(shippedOrders.isEmpty()) {
				throw new OrderNotFoundException();
			}
			orderRepository.deleteAll(shippedOrders);

	}
}

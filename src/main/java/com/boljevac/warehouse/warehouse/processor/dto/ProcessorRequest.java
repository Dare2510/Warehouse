package com.boljevac.warehouse.warehouse.processor.dto;


import com.boljevac.warehouse.warehouse.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class ProcessorRequest {

	@NotNull(message = "OrderStatus is required")
	private OrderStatus orderStatus;

	public ProcessorRequest() {
	}

	public ProcessorRequest(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

}

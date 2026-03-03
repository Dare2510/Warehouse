package com.boljevac.warehouse.warehouse.processor.dto;


import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProcessorRequest {

	@NotNull(message = "OrderStatus is required")
	private OrderStatuses orderStatuses;

	public ProcessorRequest() {
	}

	public ProcessorRequest(OrderStatuses orderStatuses) {
		this.orderStatuses = orderStatuses;
	}

	public OrderStatuses getOrderStatuses() {
		return orderStatuses;
	}

	public void setOrderStatuses(OrderStatuses orderStatuses) {
		this.orderStatuses = orderStatuses;
	}

}

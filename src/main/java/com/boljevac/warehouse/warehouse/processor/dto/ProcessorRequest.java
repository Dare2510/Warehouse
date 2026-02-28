package com.boljevac.warehouse.warehouse.processor.dto;


import com.boljevac.warehouse.warehouse.order.entity.OrderStatuses;
import jakarta.validation.constraints.NotBlank;

public class ProcessorRequest {

	@NotBlank(message = "OrderStatus is required")
	private OrderStatuses orderStatuses;


	public ProcessorRequest(OrderStatuses orderStatuses) {
		this.orderStatuses = orderStatuses;
	}


	public OrderStatuses getOrderStatus() {
		return orderStatuses;
	}

	public void setOrderStatus(OrderStatuses orderStatuses) {
		this.orderStatuses = orderStatuses;
	}

}

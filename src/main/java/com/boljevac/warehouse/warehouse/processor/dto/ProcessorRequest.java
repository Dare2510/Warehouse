package com.boljevac.warehouse.warehouse.processor.dto;


import com.boljevac.warehouse.warehouse.order.OrderStatus;

public class ProcessorRequest {

	private OrderStatus orderStatus;


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

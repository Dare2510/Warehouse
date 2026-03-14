package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;

public enum OrderStatus {
	ORDER_PLACED,
	PROCESSING,
	PACKAGED,
	SHIPPED,
	CANCELLED;

	//Validator for the correct sequence of order changes
	public boolean validatorCorrectStatusChange(OrderEntity toChange, OrderStatus orderStatus)  {
		return orderStatus == OrderStatus.PROCESSING || orderStatus == OrderStatus.CANCELLED;
	}
}




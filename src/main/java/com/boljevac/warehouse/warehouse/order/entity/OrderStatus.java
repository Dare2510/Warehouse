package com.boljevac.warehouse.warehouse.order.entity;

public enum OrderStatus {
	ORDER_PLACED,
	PROCESSING,
	PACKAGED,
	SHIPPED,
	CANCELLED;

	//Validator for the correct sequence of order changes
	public boolean validatorCorrectStatusChange(OrderEntity toChange, OrderStatus OrderStatus)  {
		boolean validStatusChange = false;
		switch(toChange.getOrderStatus()) {
			case ORDER_PLACED:
				if(OrderStatus == PROCESSING || OrderStatus == CANCELLED) {
					validStatusChange = true;
				}
				break;
			case PROCESSING:
				if(OrderStatus == PACKAGED) {
					validStatusChange = true;
				}
				break;
			case PACKAGED:
				if(OrderStatus == SHIPPED) {
					validStatusChange = true;
				}
				break;

		}
		return validStatusChange;
	}
}




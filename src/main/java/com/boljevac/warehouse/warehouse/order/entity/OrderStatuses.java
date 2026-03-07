package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;

public enum OrderStatuses {
	ORDER_PLACED,
	PROCESSING,
	PACKAGED,
	SHIPPED,
	CANCELLED;

	//Validator for the correct sequence of order changes
	public void sequenceValidator(OrderEntity toChange, OrderStatuses orderStatus) throws StatusChangeInvalidOrderException {
		switch (toChange.getOrderStatuses()) {
			case ORDER_PLACED:
				if (orderStatus == OrderStatuses.PROCESSING || orderStatus == OrderStatuses.CANCELLED) {
					toChange.setOrderStatuses(orderStatus);
				} else {
					throw new StatusChangeInvalidOrderException();
				}
				break;
			case PROCESSING:
				if (orderStatus != OrderStatuses.PACKAGED) {
					throw new StatusChangeInvalidOrderException();
				} else {
					toChange.setOrderStatuses(orderStatus);
				}
				break;
			case PACKAGED:
				if (orderStatus != OrderStatuses.SHIPPED) {
					throw new StatusChangeInvalidOrderException();
				} else {
					toChange.setOrderStatuses(orderStatus);
				}
				break;
			default:
				throw new StatusChangeInvalidOrderException();
		}
	}
}




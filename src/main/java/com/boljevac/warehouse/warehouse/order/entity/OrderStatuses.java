package com.boljevac.warehouse.warehouse.order.entity;

import com.boljevac.warehouse.warehouse.order.exception.StatusChangeInvalidOrderException;

public  enum OrderStatuses {
		ORDER_PLACED,
		PROCESSING,
		PACKAGED,
		SHIPPED,
		CANCELLED;

		public void sequenceValidator(OrderEntity toChange, OrderStatuses orderStatus) throws StatusChangeInvalidOrderException {
			switch(toChange.getStatus()) {
				case ORDER_PLACED:
					if(orderStatus == OrderStatuses.PROCESSING || orderStatus == OrderStatuses.CANCELLED) {
						toChange.setStatus(orderStatus);
					} else {
						throw new StatusChangeInvalidOrderException();
					}
					break;
				case PROCESSING:
					if(orderStatus != OrderStatuses.PACKAGED) {
						throw new StatusChangeInvalidOrderException();
					} else {
						toChange.setStatus(orderStatus);
					}
					break;
				case PACKAGED:
					if(orderStatus != OrderStatuses.SHIPPED) {
						throw new StatusChangeInvalidOrderException();
					} else {
						toChange.setStatus(orderStatus);
					}
					break;
				default:
					throw new StatusChangeInvalidOrderException();
			}
		}
	}




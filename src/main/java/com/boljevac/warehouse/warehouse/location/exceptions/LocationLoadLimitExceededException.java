package com.boljevac.warehouse.warehouse.location.exceptions;

public class LocationLoadLimitExceededException extends RuntimeException {
	public LocationLoadLimitExceededException(Long inventoryId) {
		super("The maximum weight of inventory Id " + inventoryId + " exceeds the load limit of the Location");
	}
}

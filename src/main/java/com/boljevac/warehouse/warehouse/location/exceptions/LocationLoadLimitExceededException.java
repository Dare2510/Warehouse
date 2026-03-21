package com.boljevac.warehouse.warehouse.location.exceptions;

public class LocationLoadLimitExceededException extends RuntimeException {
	public LocationLoadLimitExceededException() {
		super("The weight to store exceeds the load limit of the Location");
	}
}

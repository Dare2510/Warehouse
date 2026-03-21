package com.boljevac.warehouse.warehouse.location.exceptions;

public class LocationsNotCreatedException extends RuntimeException {
	public LocationsNotCreatedException() {
		super("Locations are not created, first create them");
	}
}

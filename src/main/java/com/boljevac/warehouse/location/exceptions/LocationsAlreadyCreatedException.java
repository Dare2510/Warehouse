package com.boljevac.warehouse.location.exceptions;

public class LocationsAlreadyCreatedException extends RuntimeException {

	public LocationsAlreadyCreatedException() {
		super("Locations already exists, cannot create new location");
	}
}

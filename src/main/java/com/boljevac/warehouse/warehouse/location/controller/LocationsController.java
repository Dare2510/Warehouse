package com.boljevac.warehouse.warehouse.location.controller;

import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/warehouse/locations")
public class LocationsController {

	private final LocationService locationService;

	public LocationsController(LocationService locationService) {
		this.locationService = locationService;
	}

	@GetMapping
	public ResponseEntity<Void> createLocation(){
		locationService.createLocations();
		return ResponseEntity.noContent().build();
	}

	@PostMapping
	public ResponseEntity<LocationsResponse> storeProduct(@RequestBody LocationsRequest locationsRequest) {
		return ResponseEntity.ok(locationService.storeInventory(locationsRequest));
	}
}

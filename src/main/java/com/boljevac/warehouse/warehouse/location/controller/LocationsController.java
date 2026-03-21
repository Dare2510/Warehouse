package com.boljevac.warehouse.warehouse.location.controller;

import com.boljevac.warehouse.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.warehouse.location.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("api/warehouse/locations")
public class LocationsController {

	private final LocationService locationService;

	public LocationsController(LocationService locationService) {
		this.locationService = locationService;
	}

	@PutMapping
	public ResponseEntity<Void> createLocation(){
		locationService.createLocations();
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping
	public ResponseEntity<LocationsResponse> storeProduct(@RequestBody LocationsRequest locationsRequest) {
		return ResponseEntity.ok(locationService.storeInventory(locationsRequest));
	}

	@GetMapping("/getAll")
	public ResponseEntity <Page<LocationsResponse>> getAllLocations(Pageable pageable){
		return ResponseEntity.ok(locationService.getInventories(pageable));
	}
}

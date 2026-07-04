package com.boljevac.warehouse.location.controller;

import com.boljevac.warehouse.location.dto.LocationsRequest;
import com.boljevac.warehouse.location.dto.LocationsResponse;
import com.boljevac.warehouse.location.service.LocationService;
import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/warehouse/locations")
@PreAuthorize("hasAnyRole('ADMIN','CLERK')")
public class LocationsController {

	private final LocationService locationService;

	public LocationsController(LocationService locationService) {
		this.locationService = locationService;
	}

	@PutMapping
	public ResponseEntity<Void> createLocation() {
		locationService.createLocations();
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping
	public ResponseEntity<LocationsResponse> storeProduct(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
														  @RequestBody @Valid LocationsRequest locationsRequest) {
		return ResponseEntity.ok(locationService.storeInventory(authenticatedUser,locationsRequest));
	}

	@GetMapping("/getAll")
	public ResponseEntity<Page<LocationsResponse>> getAllLocations(Pageable pageable) {
		return ResponseEntity.ok(locationService.getInventories(pageable));
	}
}

package com.boljevac.warehouse.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LocationsResponse {

	private Long inventoryId;
	private String product;
	private double weightPerPiece;
	private double totalWeight;
	private String location;


}

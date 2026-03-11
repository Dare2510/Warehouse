package com.boljevac.warehouse.warehouse.location.dto;

public record LocationsResponse(Long inventoryId,
								String product,
								double weightPerPiece,
								double totalWeight,
								String location) {


}

package com.boljevac.warehouse.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InventoryResponse {

	private Long inventoryId;
	private String product;
	private int quantity;
}

package com.boljevac.warehouse.warehouse.inventory.dto;

import jakarta.validation.constraints.Positive;

public class InventoryRequest {

	@Positive(message = "Product ID must be positive")
	private Long id;


//	@Enumerated(EnumType.STRING)
//	private final Aisle aisle;
//
//	@Positive(message = "Column must be positive and max 50")
//	private int rack;
//
//	@Positive(message = "Row must be positive and max 10")
//	private int levelOn;

	@Positive(message = "quantity must be > 0")
	private int quantity;

	public InventoryRequest(Long productId,int quantity) {
		this.id = productId;
//		this.aisle = aisle;
//		this.rack = rack;
//		this.levelOn = levelOn;
		this.quantity = quantity;
	}

//	public Aisle getAisle() {
//		return aisle;
//	}


	public Long getProductId() {
		return id;
	}

	public void setProductId(Long productId) {
		this.id = productId;
	}

//	public int getRack() {
//		return rack;
//	}

//	public void setRack(int rack) {
//		this.rack = rack;
//	}

//	public int getLevelOn() {
//		return levelOn;
//	}

//	public void setLevelOn(int levelOn) {
//		this.levelOn = levelOn;
//	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

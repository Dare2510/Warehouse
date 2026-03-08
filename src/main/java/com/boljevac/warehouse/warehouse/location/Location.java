package com.boljevac.warehouse.warehouse.location;

public class Location {

	final int maxWeightLocation = 500;
	private final Aisle aisle;
	private final Rack rack;
	private final LevelOn level;

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	private int quantity;


	public Location(Aisle aisle, Rack rack, LevelOn level) {
		this.aisle = aisle;
		this.rack = rack;
		this.level = level;

	}

	public int getMaxWeightLocation() {
		return maxWeightLocation;
	}

	public Aisle getAisle() {
		return aisle;
	}

	public Rack getRack() {
		return rack;
	}

	public LevelOn getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return " "+aisle + rack + level;
	}
}

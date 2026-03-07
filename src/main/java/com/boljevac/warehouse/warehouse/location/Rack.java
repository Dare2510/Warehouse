package com.boljevac.warehouse.warehouse.location;

public class Rack {

	final int firstRack = 1;
	final int lastRack = 10;
	int rack;

	public Rack(int rack) {
		this.rack = rack;
	}

	public int getFirstRack() {
		return firstRack;
	}

	public int getLastRack() {
		return lastRack;
	}

	public int getRack() {
		return rack;
	}

	public void setRack(int rack) {
		if (rack > lastRack || rack < firstRack) {
			throw new IllegalArgumentException("Invalid rack");
		}
		this.rack = rack;
	}

	@Override
	public String toString() {
		return "-"+rack;
	}
}

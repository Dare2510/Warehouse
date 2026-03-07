package com.boljevac.warehouse.warehouse.location;

public class LevelOn {

	final int maxLevel = 6;
	final int minLevel = 1;
	int  level;

	public LevelOn(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		if(level > maxLevel || level < minLevel) {
			throw new IllegalArgumentException("Invalid level");
		}
		this.level = level;
	}

	@Override
	public String toString() {
		return "-" + level;
	}
}

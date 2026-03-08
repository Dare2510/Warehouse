package com.boljevac.warehouse.warehouse.location.exceptions;

public class NoUnusedLocationException extends RuntimeException{
	public NoUnusedLocationException() {
		super("No unused location found - Warehouse is full");
	}
}

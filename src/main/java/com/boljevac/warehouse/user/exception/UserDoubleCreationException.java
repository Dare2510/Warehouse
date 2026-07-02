package com.boljevac.warehouse.user.exception;

public class UserDoubleCreationException extends RuntimeException {
	public UserDoubleCreationException(String email) {

		super("User with " + email + " already exists");
	}
}

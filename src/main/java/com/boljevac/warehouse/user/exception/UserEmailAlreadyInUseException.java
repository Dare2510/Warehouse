package com.boljevac.warehouse.user.exception;

public class UserEmailAlreadyInUseException extends RuntimeException {
	public UserEmailAlreadyInUseException(String email) {

		super("Email already in use: " + email);
	}
}

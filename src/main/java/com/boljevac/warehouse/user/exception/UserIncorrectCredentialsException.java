package com.boljevac.warehouse.user.exception;

public class UserIncorrectCredentialsException extends RuntimeException {
	public UserIncorrectCredentialsException() {
		super("Invalid password");
	}
}

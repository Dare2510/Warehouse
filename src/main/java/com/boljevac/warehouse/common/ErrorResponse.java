package com.boljevac.warehouse.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

	private final int httpStatusCode;
	private final String message;
	private final String path;
	private final LocalDateTime timestamp;


	public ErrorResponse(int httpStatusCode, String message, String path) {
		this.httpStatusCode = httpStatusCode;
		this.message = message;
		this.path = path;
		this.timestamp = LocalDateTime.now();
	}
}

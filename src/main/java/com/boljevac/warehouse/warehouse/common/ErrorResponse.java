package com.boljevac.warehouse.warehouse.common;

import java.time.LocalDateTime;

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

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}


}

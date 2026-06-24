package com.boljevac.warehouse.warehouse.security.handler;

import com.boljevac.warehouse.warehouse.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthFailedHandler implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public RestAuthFailedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request,
	                     HttpServletResponse response,
	                     AuthenticationException authException) throws IOException {

		ErrorResponse errorResponse = new ErrorResponse(
				HttpServletResponse.SC_UNAUTHORIZED,
				"Authentication failed",
				request.getRequestURI()
		);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		objectMapper.writeValue(response.getWriter(), errorResponse);
	}
}
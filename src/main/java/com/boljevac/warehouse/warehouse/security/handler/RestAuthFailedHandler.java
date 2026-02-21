package com.boljevac.warehouse.warehouse.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
@Component
public class RestAuthFailedHandler implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException, ServletException {

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("{" +
									"httpStatusCode: " + HttpServletResponse.SC_UNAUTHORIZED + "\n" +
									"message: " + authException.getMessage() + "\n" +
									"path: " + request.getRequestURI() +
									"timestamp" + LocalDateTime.now() +
									"}"

		);

	}
}

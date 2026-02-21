package com.boljevac.warehouse.warehouse.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request,
					   HttpServletResponse response,
					   AccessDeniedException accessDeniedException) throws IOException, ServletException {

			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			response.getWriter().write("{" +
				"httpStatusCode: " + HttpServletResponse.SC_FORBIDDEN+  "\n" +
				"message: " + accessDeniedException.getMessage() + "\n" +
				"path: " + request.getRequestURI() +
				"timestamp" + LocalDateTime.now() +
				"}");

	}
}

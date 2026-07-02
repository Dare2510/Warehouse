package com.boljevac.warehouse.security.auth.controller;

import com.boljevac.warehouse.security.auth.service.AuthService;
import com.boljevac.warehouse.security.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest) {
		return ResponseEntity.ok().body(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
	}


}

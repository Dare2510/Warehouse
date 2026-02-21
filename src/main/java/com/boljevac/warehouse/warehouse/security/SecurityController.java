package com.boljevac.warehouse.warehouse.security;

import com.boljevac.warehouse.warehouse.security.dto.AuthRequest;
import com.boljevac.warehouse.warehouse.security.dto.AuthResponse;
import com.boljevac.warehouse.warehouse.security.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
public class SecurityController {

	public final AuthenticationManager authenticationManager;
	public final JwtUtil jwtUtil;

	public SecurityController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getUsername(),
						request.getPassword()));

		String token = jwtUtil.generateToken(request.getUsername());


		return ResponseEntity.ok(new AuthResponse(token));

	}
}

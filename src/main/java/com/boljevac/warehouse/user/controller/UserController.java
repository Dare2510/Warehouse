package com.boljevac.warehouse.user.controller;


import com.boljevac.warehouse.security.principal.AuthenticatedUser;
import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.dto.UserResponse;
import com.boljevac.warehouse.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class UserController {

	private final UserService userService;

	@PostMapping("/register")
	public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest) {
		return ResponseEntity.ok().body(userService.registerUserByCustomer(userRequest));
	}
	@PreAuthorize("hasRole('USER')")
	@PatchMapping("/update")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UserRequest userRequest,
										   @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
		userService.updateUserByCustomer(authenticatedUser, userRequest);
		return ResponseEntity.ok().build();
	}

}
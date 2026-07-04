package com.boljevac.warehouse.user.controller;

import com.boljevac.warehouse.user.dto.UserRequest;
import com.boljevac.warehouse.user.dto.UserResponse;
import com.boljevac.warehouse.user.entity.Role;
import com.boljevac.warehouse.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLERK','ADMIN')")
public class UserManagementController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<Page<UserResponse>> getPageOfUsers(@PageableDefault(page = 0, size = 10,
			sort = "surname", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok().body(userService.getPageOfUsers(pageable));
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest) {
		return ResponseEntity.ok().body(userService.registerUserByCustomer(userRequest));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/register/{role}")
	public ResponseEntity<UserResponse> registerManagement(@RequestBody @Valid UserRequest userRequest, @PathVariable Role role) {
		return ResponseEntity.ok().body(userService.registerManagement(userRequest, role));
	}

	@PatchMapping("/{userId}/{role}/update")
	public ResponseEntity<Void> updateUser(@RequestBody @Valid UserRequest userRequest, @PathVariable Long userId, @PathVariable Role role) {
		userService.updateUserByManagement(userId, userRequest, role);
		return ResponseEntity.ok().build();
	}
}

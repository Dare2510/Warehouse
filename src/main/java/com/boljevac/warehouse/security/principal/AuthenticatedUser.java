package com.boljevac.warehouse.security.principal;

import com.boljevac.warehouse.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticatedUser {

	private Long userId;
	private String email;
	private Role role;
}

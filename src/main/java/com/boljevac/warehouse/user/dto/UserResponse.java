package com.boljevac.warehouse.user.dto;

import com.boljevac.warehouse.user.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

	private Long userId;
	private String email;
	private String username;
	private String name;
	private String surname;
	private Role role;
}

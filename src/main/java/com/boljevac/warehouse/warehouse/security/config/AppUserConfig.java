package com.boljevac.warehouse.warehouse.security.config;

import com.boljevac.warehouse.warehouse.security.service.PasswordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AppUserConfig {

	PasswordService passwordService;

	public AppUserConfig(PasswordService passwordService) {
		this.passwordService = passwordService;
	}
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User
				.withUsername("user")
				.password(passwordService.getPasswordEncoder().encode("UserPassword"))
				.roles("USER")
				.build();

		UserDetails clerk = User
				.withUsername("clerk")
				.password(passwordService.getPasswordEncoder().encode("ClerkPassword"))
				.roles("CLERK")
				.build();

		UserDetails admin = User
				.withUsername("admin")
				.password(passwordService.getPasswordEncoder().encode("AdminPassword"))
				.roles("ADMIN")
				.build();

		return new InMemoryUserDetailsManager(user,clerk,admin);
	}
}

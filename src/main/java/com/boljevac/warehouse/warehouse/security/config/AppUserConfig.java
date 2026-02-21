package com.boljevac.warehouse.warehouse.security.config;

import com.boljevac.warehouse.warehouse.security.utils.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AppUserConfig {

	SecurityUtil securityUtil;

	public AppUserConfig(SecurityUtil securityUtil) {
		this.securityUtil = securityUtil;
	}
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User
				.withUsername("user")
				.password(securityUtil.getPasswordEncoder().encode("UserPassword"))
				.roles("USER")
				.build();

		UserDetails clerk = User
				.withUsername("clerk")
				.password(securityUtil.getPasswordEncoder().encode("ClerkPassword"))
				.roles("CLERK")
				.build();

		UserDetails admin = User
				.withUsername("admin")
				.password(securityUtil.getPasswordEncoder().encode("AdminPassword"))
				.roles("ADMIN")
				.build();

		return new InMemoryUserDetailsManager(user,clerk,admin);
	}
}

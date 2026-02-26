package com.boljevac.warehouse.warehouse.security.config;

import com.boljevac.warehouse.warehouse.security.handler.RestAuthFailedHandler;
import com.boljevac.warehouse.warehouse.security.handler.RestAccessDeniedHandler;
import com.boljevac.warehouse.warehouse.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityFilter {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
												   RestAuthFailedHandler restAuthFailedHandler,
												   RestAccessDeniedHandler restAccessDeniedHandler,
												   JwtAuthFilter jwtAuthFilter) throws Exception {

		http
				.csrf(csrf -> csrf.disable())
						.sessionManagement(sessionManagement
								-> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
						.exceptionHandling(ex
								-> ex.authenticationEntryPoint(restAuthFailedHandler)
								.accessDeniedHandler(restAccessDeniedHandler))
						.authorizeHttpRequests(authorizeRequests
								->authorizeRequests
								.requestMatchers(HttpMethod.POST,"/api/warehouse/login").permitAll()
								.requestMatchers("/api/warehouse/products/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers("/api/warehouse/orders/**").hasAnyRole("ADMIN", "USER")
								.requestMatchers("/api/warehouse/processor/**").hasAnyRole("ADMIN", "CLERK")
								.anyRequest().authenticated()
						).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}
	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
		return  configuration.getAuthenticationManager();
	}
}

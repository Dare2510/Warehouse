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
								.requestMatchers(HttpMethod.GET,"/api/products/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.POST,"/api/products/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.DELETE,"/api/products/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.PUT,"/api/products/**").hasAnyRole("ADMIN", "CLERK")

								.requestMatchers(HttpMethod.GET,"/api/order/**").hasAnyRole("ADMIN", "USER")
								.requestMatchers(HttpMethod.POST,"/api/order/**").hasAnyRole("ADMIN", "USER")
								.requestMatchers(HttpMethod.PATCH,"/api/order/**").hasAnyRole("ADMIN", "USER")

								.requestMatchers(HttpMethod.GET,"/api/processor/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.POST,"/api/processor/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.DELETE,"/api/processor/**").hasAnyRole("ADMIN", "CLERK")
								.requestMatchers(HttpMethod.PUT,"/api/processor/**").hasAnyRole("ADMIN", "CLERK")
								.anyRequest().authenticated()
						).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}
	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
		return  configuration.getAuthenticationManager();
	}
}

package com.boljevac.warehouse.warehouse.security.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
 @Bean
 public PasswordEncoder getPasswordEncoder() {
	 return new BCryptPasswordEncoder();
 }
}

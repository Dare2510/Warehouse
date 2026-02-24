package com.boljevac.warehouse.warehouse.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
@Component
public class JwtToken {

	private final Key key;
	private final long duration;

	public JwtToken(@Value("${app.jwt.secret}") String secret,
					@Value("${app.jwt.expiration-ms}") long duration) {
		this.duration = duration;
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public Jws<Claims> parseToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
	}

	public String getUsernameFromToken(String token) {
		return parseToken(token).getBody().getSubject();
	}

	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public String generateToken (String username) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + duration);

		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(now)
				.setExpiration(expiration)
				.signWith(key)
				.compact();

	}


}

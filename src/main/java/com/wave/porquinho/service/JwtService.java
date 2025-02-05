package com.wave.porquinho.service;

import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {
	<T> T extractClaim(String token, Function<Claims, T> claimsResolver);

	String extractUsername(String token);

	String generateToken(UserDetails userDetails);

	String createToken(Map<String, Object> extraClaims, UserDetails userDetails);

	Long getExpiration();

	boolean isTokenValid(String token, UserDetails userDetails);

}

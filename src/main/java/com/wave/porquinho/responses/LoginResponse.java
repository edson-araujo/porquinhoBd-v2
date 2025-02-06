package com.wave.porquinho.responses;

import lombok.Data;

@Data
public class LoginResponse {
	private String token;
	private long expiresIn;
	
	public LoginResponse(String token, long expiresIn) {
		super();
		this.token = token;
		this.expiresIn = expiresIn;
	}
	
	
}

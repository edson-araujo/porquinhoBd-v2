package com.wave.porquinho.controller;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.model.User;
import com.wave.porquinho.responses.LoginResponse;
import com.wave.porquinho.service.AuthenticationService;
import com.wave.porquinho.service.JwtService;

import jakarta.mail.MessagingException;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
	private final JwtService jwtService;
	private final AuthenticationService authenticationService;
	
	public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
		this.jwtService = jwtService;
		this.authenticationService = authenticationService;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> register(@RequestBody RegisterUserDto user) throws MessagingException {
		return authenticationService.singup(user);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto user) {
		User usuarioAutenticado = authenticationService.authenticateUser(user);
		String jwtToken = jwtService.generateToken(usuarioAutenticado);
		LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpiration());
		return ResponseEntity.ok(loginResponse);
	}
	
	@PostMapping("/autenticar")
	public ResponseEntity<Map<String, String>> autenticarConta(@RequestBody VerifyUserDto user) {
		return authenticationService.verifyUser(user);
	}
	
	@PostMapping("/reenviarEmail")
	public ResponseEntity<Map<String, String>> reenviarCodigoAutenticacao(@RequestBody VerifyUserDto data) throws MessagingException {
		return authenticationService.resendVerificationCode(data.getEmail());
	}
}

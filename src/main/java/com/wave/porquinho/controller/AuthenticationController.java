package com.wave.porquinho.controller;

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
	
	@PostMapping("/singup")
	public ResponseEntity<User> register(@RequestBody RegisterUserDto user) throws MessagingException {
		return ResponseEntity.ok(authenticationService.singup(user));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto user) {
		User usuarioAutenticado = authenticationService.authenticateUser(user);
		String jwtToken = jwtService.generateToken(usuarioAutenticado);
		LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpiration());
		return ResponseEntity.ok(loginResponse);
	}
	
	@PostMapping("/autenticar")
	public ResponseEntity<?> autenticarConta(@RequestBody VerifyUserDto user) {
		try {
			authenticationService.verifyUser(user);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping("/reenviarEmail")
	public ResponseEntity<?> reenviarCodigoAutenticacao(@RequestBody String email) {
		try {
			authenticationService.resendVerificationCode(email);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}

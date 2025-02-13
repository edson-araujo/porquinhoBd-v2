package com.wave.porquinho.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wave.porquinho.dto.AlterarSenhaDto;
import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.exceptions.UserNotFoundException;
import com.wave.porquinho.exceptions.UserNotVerifiedException;
import com.wave.porquinho.model.User;
import com.wave.porquinho.responses.ApiRetornoResponse;
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
	public ResponseEntity<ApiRetornoResponse> register(@RequestBody RegisterUserDto user) throws MessagingException {
		return authenticationService.singup(user);
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody LoginUserDto user) {
	    try {
	        User usuarioAutenticado = authenticationService.authenticateUser(user);
	        String jwtToken = jwtService.generateToken(usuarioAutenticado);
	        
	        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpiration());
	        return ResponseEntity.ok(loginResponse);

	    } catch (UserNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .body(ApiRetornoResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	            
	    } catch (UserNotVerifiedException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .body(ApiRetornoResponse.of(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
	            
	    } catch (BadCredentialsException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .body(ApiRetornoResponse.of(HttpStatus.UNAUTHORIZED.value(), "Credenciais inválidas"));
	    }
	}

	
	@PostMapping("/autenticar")
	public ResponseEntity<Map<String, String>> autenticarConta(@RequestBody VerifyUserDto user) {
		return authenticationService.verifyUser(user);
	}
	
	@PostMapping("/reenviarEmail")
	public ResponseEntity<Map<String, String>> reenviarCodigoAutenticacao(@RequestBody VerifyUserDto data) throws MessagingException {
		return authenticationService.resendVerificationCode(data.getEmail());
	}
	
	@PostMapping("/esqueciSenha")
	public ResponseEntity<ApiRetornoResponse> forgotPassword(@RequestBody VerifyUserDto data) throws MessagingException {
		return authenticationService.esqueciSenha(data.getEmail());
	}

	@PostMapping("/alterarSenha")
	public ResponseEntity<ApiRetornoResponse> alterPassword(@RequestBody AlterarSenhaDto data) throws MessagingException {
		return authenticationService.alterarSenha(data);
	} 
}

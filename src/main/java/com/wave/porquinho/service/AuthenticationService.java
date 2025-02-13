package com.wave.porquinho.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.wave.porquinho.dto.AlterarSenhaDto;
import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.model.User;
import com.wave.porquinho.responses.ApiRetornoResponse;

import jakarta.mail.MessagingException;

public interface AuthenticationService {
	
	ResponseEntity<ApiRetornoResponse> singup(RegisterUserDto registerUser) throws MessagingException;
	User authenticateUser(LoginUserDto loginUser);
	ResponseEntity<Map<String, String>> verifyUser(VerifyUserDto verifyUser);
	ResponseEntity<Map<String, String>> resendVerificationCode(String email) throws MessagingException;
	ResponseEntity<ApiRetornoResponse> esqueciSenha(String email) throws MessagingException;
	ResponseEntity<ApiRetornoResponse> alterarSenha(AlterarSenhaDto alterarSenha);
}

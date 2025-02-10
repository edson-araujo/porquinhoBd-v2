package com.wave.porquinho.service;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.model.User;

import jakarta.mail.MessagingException;

public interface AuthenticationService {
	
	ResponseEntity<Map<String, String>> singup(RegisterUserDto registerUser) throws MessagingException;
	User authenticateUser(LoginUserDto loginUser);
	ResponseEntity<Map<String, String>> verifyUser(VerifyUserDto verifyUser);
	ResponseEntity<Map<String, String>> resendVerificationCode(String email) throws MessagingException;
}

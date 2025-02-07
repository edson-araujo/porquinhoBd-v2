package com.wave.porquinho.service;

import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.model.User;

import jakarta.mail.MessagingException;

public interface AuthenticationService {
	
	User singup(RegisterUserDto registerUser) throws MessagingException;
	User authenticateUser(LoginUserDto loginUser);
	void verifyUser(VerifyUserDto verifyUser);
	void resendVerificationCode(String email) throws MessagingException;
	void sendVerificationEmail(User user) throws MessagingException;
}

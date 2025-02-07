package com.wave.porquinho.service;

import java.util.Map;

import jakarta.mail.MessagingException;

public interface EmailService {

	void sendVerificationEmail(String email, String text) throws MessagingException;
	void sendMessageHtml(String to, String subject, String template, Map<String, Object> attributes) throws MessagingException;
}

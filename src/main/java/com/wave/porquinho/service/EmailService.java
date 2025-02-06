package com.wave.porquinho.service;

import org.springframework.mail.javamail.JavaMailSender;

public interface EmailService {

	JavaMailSender emailSender();

	void sendVerificationEmail(String email, String objeto, String htmlMessage);
}

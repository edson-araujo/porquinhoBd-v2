package com.wave.porquinho.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.model.User;
import com.wave.porquinho.repository.UserRepository;
import com.wave.porquinho.service.AuthenticationService;
import com.wave.porquinho.service.EmailService;
import org.springframework.http.HttpStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;
	@Value("${hostname}")
	private String hostname;

	public ResponseEntity<String> singup(RegisterUserDto registerUser) throws MessagingException {
		if (userRepository.findByEmail(registerUser.getEmail()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado.");
		}

		User user = new User(registerUser.getNome(), registerUser.getSobrenome(), registerUser.getEmail(),
				passwordEncoder.encode(registerUser.getPassword()));
		user.setCodigoVerificacao(generateVerificationCode());
		user.setExpiracaoCodigoVerificacao(LocalDateTime.now().plusMinutes(15));
		user.setVerificado(false);
		try {
			userRepository.save(user);
			sendEmail(user, "Porquinho - Verificação de Email", "registration-template", "registrationUrl",
					"/verificacao/" + user.getCodigoVerificacao());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body("Usuário cadastrado com sucesso. Verifique seu e-mail para ativação.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Erro ao salvar usuário");
		}
	}

	public User authenticateUser(LoginUserDto loginUser) {
		User user = userRepository.findByEmail(loginUser.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		if (!user.isVerificado()) {
			throw new RuntimeException("Usuário não verificado, por favor verifique seu e-mail.");
		}
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword()));

		return user;
	}

	public void verifyUser(VerifyUserDto verifyUser) {
		Optional<User> optionalUser = userRepository.findByEmail(verifyUser.getEmail());
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (user.getExpiracaoCodigoVerificacao().isBefore(LocalDateTime.now())) {
				throw new RuntimeException("Código de verificação expirado");
			}

			if (user.getCodigoVerificacao().equals(verifyUser.getCodigoVerificacao())) {
				user.setVerificado(true);
				user.setCodigoVerificacao(null);
				user.setExpiracaoCodigoVerificacao(null);
				userRepository.save(user);
			} else {
				throw new RuntimeException("Código de verificação inválido");
			}
		} else {
			throw new RuntimeException("Usuário não encontrado");
		}
	}

	public void resendVerificationCode(String email) throws MessagingException {
		Optional<User> user = userRepository.findByEmail(email);
		if (user.isPresent()) {
			User userEntity = user.get();
			if (userEntity.isVerificado()) {
				throw new RuntimeException("Usuário já verificado");
			}
			userEntity.setCodigoVerificacao(generateVerificationCode());
			userEntity.setExpiracaoCodigoVerificacao(LocalDateTime.now().plusMinutes(15));
			sendEmail(userEntity, "Porquinho - Verificação de Email", "email/verification-email", "urlVerificacao",
					"/verificacao/" + userEntity.getCodigoVerificacao());
			userRepository.save(userEntity);
		} else {
			throw new RuntimeException("Usuário não encontrado");
		}
	}

	private String generateVerificationCode() {
		Random random = new Random();
		int code = random.nextInt(900000) + 100000;
		return String.valueOf(code);
	}

	public void sendVerificationEmail(User user) {
		String objeto = "Verificação de e-mail";
		String codigoVerificacao = "<h1>" + user.getCodigoVerificacao() + "</h1>";
		String htmlMessage = "<html>" + "<body style=\"font-family: Arial, sans-serif;\">"
				+ "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
				+ "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
				+ "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
				+ "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
				+ "<h3 style=\"color: #333;\">Verification Code:</h3>"
				+ "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + codigoVerificacao + "</p>"
				+ "</div>" + "</div>" + "</body>" + "</html>";
		try {
			emailService.sendVerificationEmail(user.getEmail(), htmlMessage);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Erro ao enviar e-mail de verificação");
		}
	}

	private void sendEmail(User user, String subject, String template, String urlAttribute, String urlPath)
			throws MessagingException {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("nome", user.getNome());
		attributes.put("codigoVerificacao", user.getCodigoVerificacao());
		attributes.put(urlAttribute, "http://" + hostname + urlPath);
		emailService.sendMessageHtml(user.getEmail(), subject, template, attributes);
	}

}

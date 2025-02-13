package com.wave.porquinho.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wave.porquinho.dto.AlterarSenhaDto;
import com.wave.porquinho.dto.LoginUserDto;
import com.wave.porquinho.dto.RegisterUserDto;
import com.wave.porquinho.dto.VerifyUserDto;
import com.wave.porquinho.exceptions.UserNotFoundException;
import com.wave.porquinho.exceptions.UserNotVerifiedException;
import com.wave.porquinho.model.User;
import com.wave.porquinho.repository.UserRepository;
import com.wave.porquinho.responses.ApiRetornoResponse;
import com.wave.porquinho.service.AuthenticationService;
import com.wave.porquinho.service.EmailService;
import com.wave.porquinho.service.JwtService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;
	private final JwtService jwtService;

	@Value("${hostname}")
	private String hostname;

	public ResponseEntity<ApiRetornoResponse> singup(RegisterUserDto registerUser) throws MessagingException {
		Optional<User> existingUser = userRepository.findByEmail(registerUser.getEmail());

		if (existingUser.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(ApiRetornoResponse.withFields(HttpStatus.CONFLICT.value(),
							"Erro de validação. Revise seus dados.", Map.of("email", "E-mail já cadastrado.")));
		}

		User user = new User(registerUser.getNome(), registerUser.getSobrenome(), registerUser.getEmail(),
				passwordEncoder.encode(registerUser.getPassword()));
		user.setCodigoVerificacao(generateVerificationCode());
		user.setExpiracaoCodigoVerificacao(LocalDateTime.now().plusMinutes(10));
		user.setVerificado(false);

		try {
			userRepository.save(user);
			sendEmail(user, "Porquinho - Verificação de Email", "registration-template", "registrationUrl",
					"/verificacao/" + user.getCodigoVerificacao());

			return ResponseEntity.status(HttpStatus.CREATED)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(ApiRetornoResponse.of(HttpStatus.CREATED.value(),
							"Usuário cadastrado com sucesso. Verifique seu e-mail para ativação."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(ApiRetornoResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
							"Erro ao salvar usuário, tente novamente mais tarde."));
		}
	}

	public User authenticateUser(LoginUserDto loginUser) {
		Optional<User> userOptional = userRepository.findByEmail(loginUser.getEmail());

		if (userOptional.isEmpty()) {
			throw new UserNotFoundException("Conta não cadastrada");
		}

		User user = userOptional.get();

		if (!user.isVerificado()) {
			throw new UserNotVerifiedException("Usuário não verificado, por favor verifique seu e-mail.");
		}

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword()));

		return user;
	}

	public ResponseEntity<Map<String, String>> verifyUser(VerifyUserDto verifyUser) {
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
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.body(Map.of("message", "Usuário autenticado com sucesso."));
			} else {
				throw new RuntimeException("Código de verificação inválido");
			}
		} else {
			throw new RuntimeException("Usuário não encontrado");
		}
	}

	public ResponseEntity<Map<String, String>> resendVerificationCode(String email) throws MessagingException {
		Optional<User> user = userRepository.findByEmail(email.replace("\"", ""));
		if (user.isPresent()) {
			User userEntity = user.get();
			if (userEntity.isVerificado()) {
				throw new RuntimeException("Usuário já verificado");
			}
			userEntity.setCodigoVerificacao(generateVerificationCode());
			userEntity.setExpiracaoCodigoVerificacao(LocalDateTime.now().plusMinutes(10));
			sendEmail(userEntity, "Porquinho - Verificação de Email", "registration-template", "registrationUrl",
					"verificacao/" + userEntity.getCodigoVerificacao());
			userRepository.save(userEntity);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(Map.of("message", "E-mail enviado com sucesso!"));
		} else {
			throw new RuntimeException("Usuário não encontrado");
		}
	}

	private String generateVerificationCode() {
		Random random = new Random();
		int code = random.nextInt(900000) + 100000;
		return String.valueOf(code);
	}

	private void sendEmail(User user, String subject, String template, String urlAttribute, String urlPath)
			throws MessagingException {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("nome", user.getNome());
		attributes.put("codigoVerificacao", user.getCodigoVerificacao());
		attributes.put(urlAttribute, "http://" + hostname + urlPath);
		emailService.sendMessageHtml(user.getEmail(), subject, template, attributes);
	}

	public ResponseEntity<ApiRetornoResponse> esqueciSenha(String email) throws MessagingException {
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isEmpty()) {
			throw new UserNotFoundException("E-mail não cadastrado.");
		}

		User user = userOptional.get();
		String resetToken = generateVerificationCode();
		user.setCodigoVerificacao(resetToken);
		user.setVerificado(false);
		user.setExpiracaoCodigoVerificacao(LocalDateTime.now().plusMinutes(10));
		userRepository.save(user);

		sendEmail(user, "Porquinho - Alteração de senha", "alterarSenha-template", "resetSenhaUrl",
				user.getCodigoVerificacao() + "/alterarSenha/");

		return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.body(ApiRetornoResponse.of(HttpStatus.OK.value(), "E-mail enviado com sucesso"));
	}

	public ResponseEntity<ApiRetornoResponse> alterarSenha(AlterarSenhaDto data) {
		Optional<User> user = userRepository.findByCodigoVerificacao(data.getCodigoVerificacao());
		try {
			if (user.isPresent()) {
				User user2 = user.get();
				if (user2.getCodigoVerificacao().equals(data.getCodigoVerificacao())) {
					if (user2.getExpiracaoCodigoVerificacao().isAfter(LocalDateTime.now())) {
						if (passwordEncoder.matches(data.getNovaSenha(), user2.getPassword())) {
							return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
									.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
									.body(ApiRetornoResponse.of(HttpStatus.UNAUTHORIZED.value(),
											"A nova senha não pode ser igual a senhas anteriores"));
						}
						user2.setPassword(passwordEncoder.encode(data.getNovaSenha()));
						user2.setCodigoVerificacao(null);
						user2.setExpiracaoCodigoVerificacao(null);
						userRepository.save(user2);
						return ResponseEntity.status(HttpStatus.OK)
								.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
								.body(ApiRetornoResponse.of(HttpStatus.OK.value(), "Senha alterada com sucesso"));
					} else {
						return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
								.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
								.body(ApiRetornoResponse.of(HttpStatus.UNAUTHORIZED.value(),
										"Código de autenticação expirado"));
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(ApiRetornoResponse
									.of(HttpStatus.UNAUTHORIZED.value(), "Código de autenticação inválido"));
				}
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(ApiRetornoResponse
								.of(HttpStatus.UNAUTHORIZED.value(), "Código de autenticação inválido"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.body(ApiRetornoResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
							"Erro ao alterar a senha"));
		}

	}
}

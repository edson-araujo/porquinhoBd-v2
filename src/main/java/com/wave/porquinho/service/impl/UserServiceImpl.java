package com.wave.porquinho.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wave.porquinho.model.User;
import com.wave.porquinho.repository.UserRepository;
import com.wave.porquinho.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> allUsers() {
		return userRepository.findAll();
	}

	@Override
	public Optional<User> getUserByCodigoVerificacao(String codigoAutenticacao) {
		return userRepository.findByCodigoVerificacao(codigoAutenticacao);
	}
}

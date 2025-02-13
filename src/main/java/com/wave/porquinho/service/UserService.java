package com.wave.porquinho.service;

import java.util.Optional;

import com.wave.porquinho.model.User;

public interface UserService {

	Optional<User> getUserByCodigoVerificacao(String codigoAutenticacao);
}

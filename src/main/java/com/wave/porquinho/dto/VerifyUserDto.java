package com.wave.porquinho.dto;

import lombok.Data;

@Data
public class VerifyUserDto {

	private String email;
	private String codigoVerificacao;
}

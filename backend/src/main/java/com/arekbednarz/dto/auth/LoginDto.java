package com.arekbednarz.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
public @Data class LoginDto {

	@NotNull
	@NotEmpty
	@JsonProperty("email")
	private String email;

	@NotNull
	@NotEmpty
	@JsonProperty("password")
	private String password;
}

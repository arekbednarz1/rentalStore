package com.arekbednarz.dto.tokenMgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public @Data class RegisterUserDto {

	@NotNull
	@NotEmpty
	@JsonProperty("username")
	private String username;

	@NotNull
	@NotEmpty
	@JsonProperty("password")
	private String password;

	@NotNull
	@NotEmpty
	@JsonProperty("email")
	private String email;
}

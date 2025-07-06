package com.arekbednarz.dto.userMgmt;

import com.arekbednarz.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class UserDto {

	@NotNull
	@NotEmpty
	@JsonProperty("name")
	private String name;

	@NotNull
	@NotEmpty
	@JsonProperty("email")
	private String email;

	@NotNull
	@NotEmpty
	@JsonProperty("role")
	private Role role;

	@NotNull
	@NotEmpty
	@JsonProperty("password")
	private String password;
}

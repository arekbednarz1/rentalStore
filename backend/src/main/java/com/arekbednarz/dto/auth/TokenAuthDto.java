package com.arekbednarz.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;


@Builder
public @Data class TokenAuthDto {
	@JsonProperty("accessToken")
	private String accessToken;

	@JsonProperty("refreshToken")
	private String refreshToken;
}

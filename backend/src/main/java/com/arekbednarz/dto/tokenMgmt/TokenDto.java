package com.arekbednarz.dto.tokenMgmt;

import com.arekbednarz.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public @Data class TokenDto {
	private String token;
	private User user;
}

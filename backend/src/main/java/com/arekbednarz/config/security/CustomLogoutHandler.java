package com.arekbednarz.config.security;

import com.arekbednarz.dto.tokenMgmt.TokenUpdateDto;
import com.arekbednarz.enums.TokenUpdateAction;
import com.arekbednarz.service.IManageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class CustomLogoutHandler implements LogoutHandler {
	private final IManageService tokenService;

	@Autowired
	public CustomLogoutHandler(@Qualifier("tokenCreationService") IManageService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	public void logout(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) {
		final String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		tokenService.update(
			new TokenUpdateDto(null, authHeader.substring(7), Set.of(TokenUpdateAction.REVOKE)));
		SecurityContextHolder.clearContext();
	}
}

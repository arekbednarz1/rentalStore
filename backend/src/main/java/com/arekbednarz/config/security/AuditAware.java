package com.arekbednarz.config.security;

import com.arekbednarz.model.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


public class AuditAware implements AuditorAware<Long> {
	@Override
	public Optional<Long> getCurrentAuditor() {
		Authentication authentication =
			SecurityContextHolder
				.getContext()
				.getAuthentication();
		if (authentication == null ||
			!authentication.isAuthenticated() ||
			authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}

		User userPrincipal = (User) authentication.getPrincipal();
		return Optional.ofNullable(userPrincipal.getId());
	}
}

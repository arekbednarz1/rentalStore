package com.arekbednarz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;


@AllArgsConstructor
public enum Role {
	USER(Set.of("self:manage", "movie:list", "rent:manage")),
	ADMIN(Set.of("user:create", "self:manage", "movie:list", "movie:manage"));

	@Getter
	private final Set<String> permissions;

	public List<SimpleGrantedAuthority> getAuthorities() {
		var authorities =
			new java.util.ArrayList<>(getPermissions()
				.stream()
				.map(SimpleGrantedAuthority::new)
				.toList());
		authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
		return authorities;
	}
}

package com.arekbednarz.service.impl.auth;

import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.tokenMgmt.TokenDto;
import com.arekbednarz.dto.tokenMgmt.TokenUpdateDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.enums.EntityType;
import com.arekbednarz.enums.TokenUpdateAction;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.service.IAuthService;
import com.arekbednarz.service.IManageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


@Service
public class AuthService implements IAuthService {
	private static final Logger LOG = Logger.getLogger(AuthService.class);

	private final Map<EntityType, IManageService> creationServiceMap;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public AuthService(
		@Qualifier("userCreationService") IManageService userCreationService,
		@Qualifier("tokenCreationService") IManageService tokenCreationService,
		JwtService jwtService,
		AuthenticationManager authenticationManager) {
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.creationServiceMap = Map.of(
			EntityType.USER, userCreationService,
			EntityType.TOKEN, tokenCreationService);
	}

	public TokenAuthDto registerUser(UserDto userDto) {
		LOG.info("Registering user: " + userDto.getEmail());
		final User user = creationServiceMap.get(EntityType.USER).create(userDto);
		final String token = jwtService.generateToken(user);
		final String refreshToken = jwtService.generateRefreshToken(user);
		createToken(token, user);

		return TokenAuthDto.builder()
			.accessToken(token)
			.refreshToken(refreshToken)
			.build();
	}

	public TokenAuthDto authenticate(final LoginDto request) {
		LOG.info("Authenticating user: " + request.getEmail());
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		User user = getUserByEmail(request.getEmail());

		var token = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);

		revokeAllUserTokens(user);
		createToken(token, user);

		return TokenAuthDto.builder()
			.accessToken(token)
			.refreshToken(refreshToken)
			.build();
	}

	public void refreshToken(
		HttpServletRequest request,
		HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			User user = getUserByEmail(userEmail);
			if (jwtService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtService.generateToken(user);
				revokeAllUserTokens(user);
				createToken(accessToken, user);
				var authResponse = TokenAuthDto.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

	private User getUserByEmail(String email) {
		return creationServiceMap.get(EntityType.USER).getOne(email);
	}

	private void createToken(final String token, final User user) {
		creationServiceMap.get(EntityType.TOKEN).create(
			TokenDto.builder()
				.token(token)
				.user(user)
				.build());
	}

	private void revokeAllUserTokens(final User user) {
		creationServiceMap.get(EntityType.TOKEN).update(new TokenUpdateDto(user.getId(), null, Set.of(TokenUpdateAction.REVOKE)));
	}
}

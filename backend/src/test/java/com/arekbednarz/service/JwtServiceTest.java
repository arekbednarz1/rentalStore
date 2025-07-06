package com.arekbednarz.service;

import com.arekbednarz.enums.Role;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.service.impl.auth.JwtService;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	private final Faker FAKER = new Faker();

	@Test
	void shouldGenerateTokenAndExtractUser() {
		UserDetails userDetails = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.leagueOfLegends().champion())
			.build();

		var token = jwtService.generateToken(userDetails);

		assertNotNull(token);

		var usernameFromToken = jwtService.extractUsername(token);

		assertNotNull(usernameFromToken);

		assertEquals(userDetails.getUsername(), usernameFromToken);
	}

	@Test
	void shouldGenerateRefreshTokenAndExtractUser() {
		UserDetails userDetails = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.leagueOfLegends().champion())
			.build();

		var token = jwtService.generateRefreshToken(userDetails);

		assertNotNull(token);

		var usernameFromToken = jwtService.extractUsername(token);

		assertNotNull(usernameFromToken);

		assertEquals(userDetails.getUsername(), usernameFromToken);
	}

	@Test
	void shouldCheckIsTokenValid() {
		UserDetails userDetails = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.leagueOfLegends().champion())
			.build();

		var token = jwtService.generateRefreshToken(userDetails);

		assertTrue(jwtService.isTokenValid(token, userDetails));

		UserDetails userDetailsIncorrect = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.zelda().game())
			.build();

		assertFalse(jwtService.isTokenValid(token, userDetailsIncorrect));
	}

}

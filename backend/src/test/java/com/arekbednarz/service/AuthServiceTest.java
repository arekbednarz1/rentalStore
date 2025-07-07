package com.arekbednarz.service;

import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.service.impl.auth.AuthService;
import com.arekbednarz.service.impl.auth.JwtService;
import com.arekbednarz.utils.PostgresqlTestContainer;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtService jwtService;

	private final Faker FAKER = new Faker();

	@Test
	@Order(1)
	void shouldRegisterUser() {
		var userDto = UserDto.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.name(FAKER.backToTheFuture().character())
			.password("test")
			.build();

		TokenAuthDto tokenAuthDto = authService.registerUser(userDto);
		assertNotNull(tokenAuthDto);

		var tokenUsername = jwtService.extractUsername(tokenAuthDto.getAccessToken());

		assertNotNull(tokenUsername);
		assertEquals(userDto.getEmail(), tokenUsername);
	}

	@Test
	@Order(2)
	void shouldRegisterAdminAndNextAuthenticate() {
		var userDto = UserDto.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.ADMIN)
			.name(FAKER.backToTheFuture().character())
			.password("test")
			.build();

		authService.registerUser(userDto);

		TokenAuthDto tokenAuthDto = authService.authenticate(new LoginDto(userDto.getEmail(), userDto.getPassword()));

		assertNotNull(tokenAuthDto);

		var tokenUsername = jwtService.extractUsername(tokenAuthDto.getAccessToken());

		assertNotNull(tokenUsername);
		assertEquals(userDto.getEmail(), tokenUsername);
	}

}

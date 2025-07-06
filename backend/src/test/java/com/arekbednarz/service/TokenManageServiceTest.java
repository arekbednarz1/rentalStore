package com.arekbednarz.service;

import com.arekbednarz.dto.tokenMgmt.TokenDto;
import com.arekbednarz.dto.tokenMgmt.TokenUpdateDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.enums.TokenType;
import com.arekbednarz.enums.TokenUpdateAction;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.Token;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.repository.TokenRepository;
import com.arekbednarz.model.repository.UserRepository;
import com.arekbednarz.utils.PostgresqlTestContainer;
import com.github.javafaker.Faker;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TokenManageServiceTest {

	@MockitoSpyBean
	@Qualifier("tokenCreationService")
	private IManageService tokenService;

	@MockitoSpyBean
	private TokenRepository tokenRepository;

	@MockitoSpyBean
	private UserRepository userRepository;

	private final Faker FAKER = new Faker();

	@Test
	@Order(1)
	void shouldCreateToken() {
		var user = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.leagueOfLegends().champion())
			.build();

		userRepository.save(user);

		var dto = getTokenDtoObject(user);

		Token token = tokenService.create(dto);

		assertNotNull(token);
		assertNotNull(token.getId());
		assertEquals(token.getToken(), dto.getToken());
		assertEquals(token.getUser().getId(), dto.getUser().getId());

	}

	@Test
	@Order(2)
	void shouldCorrectlyGetTokenByUserEmail() {
		var token = Token.builder()
			.id(2L)
			.tokenType(TokenType.BEARER)
			.revoked(false)
			.expired(false)
			.token(FAKER.lorem().characters(20, 30))
			.user(new User())
			.build();

		when(tokenRepository.findByToken(anyString())).thenReturn(Optional.ofNullable(token));

		assert token != null;
		var tokenDb = ((Token) tokenService.getOne(token.getToken()));

		assertNotNull(tokenDb);
		assertNotNull(tokenDb.getId());
		assertEquals(tokenDb.getToken(), token.getToken());
	}

	@Test
	@Order(3)
	void shouldCorrectlyGetAllTokens() {
		var token = Token.builder()
			.id(2L)
			.tokenType(TokenType.BEARER)
			.revoked(false)
			.expired(false)
			.token(FAKER.lorem().characters(20, 30))
			.user(new User())
			.build();

		when(tokenRepository.findAll()).thenReturn(List.of(token));

		List<Token> tokenDb = tokenService.getAll();

		assertNotNull(tokenDb);
		assertEquals(tokenDb.size(), 1);
	}

	@Test
	@Order(4)
	void shouldRevokeTokens() {
		var user = User.builder()
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.username(FAKER.leagueOfLegends().champion())
			.build();

		var userDb = userRepository.save(user);

		var dto = getTokenDtoObject(user);

		Token token = tokenService.create(dto);

		TokenUpdateDto updateDto = new TokenUpdateDto(userDb.getId(), null, Set.of(TokenUpdateAction.REVOKE));

		tokenService.update(updateDto);

		Token tokenAfterUpdate = tokenService.getOne(token.getToken());

		assertNotNull(tokenAfterUpdate);
		assertEquals(tokenAfterUpdate.getToken(), token.getToken());
		assertTrue(tokenAfterUpdate.isRevoked());
		assertTrue(tokenAfterUpdate.isExpired());
	}

	@Test
	@Order(5)
	void shouldThrowExceptionWhenTryToDeleteToken() {
		assertThrows(BadRequestException.class, () -> tokenService.delete(FAKER.internet().avatar()));
	}

	@Test
	@Order(6)
	void shouldThrowExceptionWhenTokenNotFound() {
		assertThrows(EntityNotFoundException.class, () -> tokenService.getOne(FAKER.internet().avatar()));
	}

	private TokenDto getTokenDtoObject(final User user) {
		return TokenDto.builder()
			.token(FAKER.lorem().characters(20, 40))
			.user(user)
			.build();
	}
}

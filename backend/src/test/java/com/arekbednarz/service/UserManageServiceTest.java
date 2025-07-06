package com.arekbednarz.service;

import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.dto.userMgmt.UserUpdateDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.enums.UserUpdateAction;
import com.arekbednarz.exception.EntityCreationException;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.repository.UserRepository;
import com.arekbednarz.utils.PostgresqlTestContainer;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManageServiceTest {

	@MockitoSpyBean
	@Qualifier("userCreationService")
	private IManageService userManageService;

	@MockitoSpyBean
	private UserRepository userRepository;

	private final Faker FAKER = new Faker();

	@Test
	@Order(1)
	void shouldCreateUser() {
		var dto = getUserDtoObject();
		var user = ((User) userManageService.create(dto));

		assertNotNull(user);
		assertEquals(user.getRole(), Role.USER);
		assertEquals(user.getEmail(), dto.getEmail());
		assertEquals(user.getUsername(), dto.getName());

		assertNotNull(userManageService.getOne(dto.getEmail()));
	}

	@Test
	@Order(2)
	void shouldCorrectlyGetUser() {
		var user = User.builder()
			.id(2L)
			.username(FAKER.harryPotter().character())
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.build();

		when(userRepository.findUserByEmail(anyString())).thenReturn(user);

		var userDb = ((User) userManageService.getOne(user.getEmail()));

		assertNotNull(userDb);
		assertEquals(user.getEmail(), userDb.getEmail());
		assertEquals(user.getPassword(), userDb.getPassword());
		assertEquals(user.getRole(), userDb.getRole());
		assertEquals(user.getUsername(), userDb.getUsername());
		assertEquals(user.getId(), userDb.getId());
	}

	@Test
	@Order(3)
	void shouldCorrectlyGetAllUsers() {
		var user = User.builder()
			.id(3L)
			.username(FAKER.zelda().character())
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.build();

		when(userRepository.findAll()).thenReturn(List.of(user));

		List<UserDto> users = userManageService.getAll();
		assertNotNull(users);
		assertFalse(users.isEmpty());
		assertEquals(1, users.size());
	}

	@Test
	@Order(4)
	void shouldUpdateUser() {
		var dto = getUserDtoObject();
		var user = ((User) userManageService.create(dto));

		var updatedUserName = "newUserName";
		var updatedRole = Role.ADMIN;
		var updatedPassword = FAKER.internet().password();

		var updateDto = new UserUpdateDto(dto.getEmail(),
			Set.of(
				UserUpdateAction.PASSWORD,
				UserUpdateAction.NAME,
				UserUpdateAction.ROLE), updatedUserName, updatedPassword, updatedRole);

		userManageService.update(updateDto);

		User updatedUser = userManageService.getOne(user.getEmail());

		assertEquals(updatedUser.getRole(), updatedRole);
		assertEquals(updatedUser.getUsername(), updatedUserName);
		assertNotEquals(updatedUser.getPassword(), user.getPassword());
	}

	@Test
	@Order(5)
	void shouldDeleteUser() {
		var dto = getUserDtoObject();
		var user = ((User) userManageService.create(dto));

		var userBeforeDelete = userManageService.getOne(user.getEmail());

		assertNotNull(userBeforeDelete);

		userManageService.delete(dto.getEmail());

		assertThrows(EntityNotFoundException.class, () -> userManageService.getOne(user.getEmail()));
	}

	@Test
	@Order(6)
	void shouldThrowExceptionWhenUserExists() {
		var dto = getUserDtoObject();
		userManageService.create(dto);
		assertThrows(EntityCreationException.class, () -> userManageService.create(dto));
	}

	@Test
	@Order(7)
	void shouldThrowExceptionWhenUserNotExists() {
		assertThrows(EntityNotFoundException.class, () -> userManageService.getOne(FAKER.internet().emailAddress()));
	}

	@Test
	@Order(8)
	void shouldThrowExceptionWhenUpdateDtoIsIncorrect() {
		var user = User.builder()
			.id(3L)
			.username(FAKER.zelda().character())
			.email(FAKER.internet().emailAddress())
			.role(Role.USER)
			.password(FAKER.internet().password())
			.build();

		when(userRepository.findUserByEmail(anyString())).thenReturn(user);

		var updateDto = new UserUpdateDto(user.getEmail(),
			Set.of(
				UserUpdateAction.NAME), null, "updatedPassword", Role.USER);

		Exception exception = assertThrows(EntityCreationException.class, () -> userManageService.update(updateDto));

		assertTrue(exception.getMessage().contains("Update source cannot be null"));
	}

	private UserDto getUserDtoObject() {
		return UserDto.builder()
			.name(FAKER.name().name())
			.role(Role.USER)
			.email(FAKER.internet().emailAddress())
			.password("TEST")
			.build();
	}
}

package com.arekbednarz.controller;

import com.arekbednarz.BackendApplication;
import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.tokenMgmt.RegisterUserDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.enums.Role;
import com.arekbednarz.utils.KafkaTestContainer;
import com.arekbednarz.utils.PostgresqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ContextConfiguration(initializers = { PostgresqlTestContainer.class, KafkaTestContainer.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = BackendApplication.class)
class UserManageControllerTest {

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	// @formatter:off
    @SuppressWarnings("serial")
    private static final Type USERS_LIST = new ArrayList<UserDto>() {}.getClass().getGenericSuperclass();
    // @formatter:on

	@Test
	void shouldGetSelfUser() {
		var token = getToken("noobuser@email.com", "test").getAccessToken();

		// @formatter:off
        var response =
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/users/self")
            .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);
        // @formatter:on

		assertEquals("noobuser@email.com", response.getEmail());
		assertEquals("USER", response.getRole().name());
	}

	@Test
	void shouldUpdateSelfUser() {
		var token = getToken("noobuser@email.com", "test").getAccessToken();

		// @formatter:off
        var response =
            given()
                .header("Authorization", "Bearer " + token)
                .param("name", "UpdatedName")
            .when()
                .put("/api/v1/users/self")
            .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);
        // @formatter:on

		assertEquals("UpdatedName", response.getName());
	}

	@Test
	void shouldUpdateOtherUserByAdmin() {
		var token = getToken("admin@email.com", "test").getAccessToken();
		RegisterUserDto registerUserDto = new RegisterUserDto("arek", "pass123", "arek@tests.pl");

		// @formatter:off
            given()
                .contentType(ContentType.JSON)
                .body(registerUserDto)
            .when()
                .post("/api/v1/auth/register")
            .then()
                .statusCode(200);

        var response =
            given()
                .header("Authorization", "Bearer " + token)
                .param("email", "arek@tests.pl")
                .param("role", Role.ADMIN.name())
            .when()
                .put("/api/v1/users")
            .then()
                .statusCode(200)
                .extract().as(UserDto.class);
        // @formatter:on

		assertEquals("ADMIN", response.getRole().name());
	}

	@Test
	void shouldNotDeleteSelf() {
		var token = getToken("admin@email.com", "test").getAccessToken();

		// @formatter:off
        given()
            .header("Authorization", "Bearer " + token)
            .param("email", "admin@email.com")
        .when()
            .delete("/api/v1/users")
        .then()
            .statusCode(400);
        // @formatter:on
	}

	@Test
	void shouldDeleteOtherUser() {
		var token = getToken("admin@email.com", "test").getAccessToken();

		RegisterUserDto registerUserDto = new RegisterUserDto("arek", "pass123", "arek@tests32.pl");

		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(registerUserDto)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + token)
            .param("email", "arek@tests32.pl")
        .when()
            .delete("/api/v1/users")
        .then()
            .statusCode(204);
        // @formatter:on
	}

	@Test
	void shouldGetUserByEmail() {
		var token = getToken("admin@email.com", "test").getAccessToken();

		// @formatter:off
        var response =
            given()
                .header("Authorization", "Bearer " + token)
                .param("email", "admin@email.com")
            .when()
                .get("/api/v1/users")
            .then()
                .statusCode(200)
                .extract()
                .as(UserDto.class);
        // @formatter:on

		assertEquals("admin@email.com", response.getEmail());
	}

	@Test
	void shouldGetUserList() {
		var token = getToken("admin@email.com", "test").getAccessToken();

		// @formatter:off
        List<UserDto> users =
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/users/list")
            .then().statusCode(200)
                .extract().as(USERS_LIST);
        // @formatter:on

		assertFalse(users.isEmpty());
	}

	@Test
	void shouldThrow403ForUserListAccessAsUser() {
		var token = getToken("noobuser@email.com", "test").getAccessToken();

		// @formatter:off
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/v1/users/list")
        .then()
            .statusCode(403);
        // @formatter:on
	}

	private TokenAuthDto getToken(String username, String password) {
		LoginDto loginDto = new LoginDto(username, password);
		return given()
			.contentType(ContentType.JSON)
			.body(loginDto)
			.post("/api/v1/auth/authenticate")
			.then().statusCode(200)
			.extract().as(TokenAuthDto.class);
	}
}

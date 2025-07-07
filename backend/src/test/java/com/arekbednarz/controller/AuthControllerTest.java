package com.arekbednarz.controller;

import com.arekbednarz.BackendApplication;
import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.tokenMgmt.RegisterUserDto;
import com.arekbednarz.utils.KafkaTestContainer;
import com.arekbednarz.utils.PostgresqlTestContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ContextConfiguration(initializers = { PostgresqlTestContainer.class, KafkaTestContainer.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = BackendApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@Test
	void shouldRegisterUser() {
		RegisterUserDto registerUserDto = new RegisterUserDto("arek", "pass123", "arek@ao.pl");

		// @formatter:off
        var response =
        given()
            .contentType(ContentType.JSON)
            .body(registerUserDto)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(200)
        .extract()
        .response()
        .as(TokenAuthDto.class);
        // @formatter:on

		assertNotNull(response);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getRefreshToken());
	}

	@Test
	void shouldAuthenticateAdmin() {
		LoginDto loginDto = new LoginDto("admin@email.com", "test");

		// @formatter:off
        var response =
            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
            .when()
                .post("/api/v1/auth/authenticate")
            .then()
                .statusCode(200)
                .extract()
                .response()
                .as(TokenAuthDto.class);
        // @formatter:on

		assertNotNull(response);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getRefreshToken());
	}

	@Test
	void shouldCallRefreshToken() {
		LoginDto loginDto = new LoginDto("admin@email.com", "test");

		// @formatter:off
        var responseAuth =
            given()
                .contentType(ContentType.JSON)
                .body(loginDto)
            .when()
                .post("/api/v1/auth/authenticate")
            .then()
                .statusCode(200)
                .extract()
                .response()
                .as(TokenAuthDto.class);
        // @formatter:on

		assertNotNull(responseAuth);
		var refreshToken = responseAuth.getRefreshToken();

		// @formatter:off
            given()
                .header("Authorization", "Bearer " + refreshToken)
                .body(loginDto)
            .when()
                .post("/api/v1/auth/refresh-token")
            .then()
                .statusCode(200);
        // @formatter:on
	}
}

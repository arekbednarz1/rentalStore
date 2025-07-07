package com.arekbednarz.controller;

import com.arekbednarz.BackendApplication;
import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.tokenMgmt.RegisterUserDto;
import com.arekbednarz.enums.Genre;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ContextConfiguration(initializers = { PostgresqlTestContainer.class, KafkaTestContainer.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = BackendApplication.class)
@AutoConfigureMockMvc
class MovieManageControllerTest {

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@SuppressWarnings("serial")
	private static final Type MOVIE_DTO_LIST = new ArrayList<MovieDto>() {
	}
		.getClass()
		.getGenericSuperclass();

	@Test
	void shouldCreateNextEditAndFinallyRemoveMovie() {
		MovieDto dto = MovieDto.builder()
			.genre(Genre.DRAMA)
			.available(true)
			.title("test")
			.build();

		var token = getToken("admin@email.com", "test").getAccessToken();

		// @formatter:off
        var responseCreate =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
            .when()
                .post("/api/v1/movies")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(MovieDto.class);

        var responseUpdate =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .param("id",responseCreate.getId())
                .param("title","DUMMY")
                .param("genre",Genre.COMEDY)
                .param("status",true)
            .when()
                .put("/api/v1/movies")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(MovieDto.class);

        List<MovieDto> responseList =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/movies/list")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(MOVIE_DTO_LIST);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/v1/movies/"+responseCreate.getId())
        .then()
            .statusCode(204);

        List<MovieDto> responseListAfterDelete =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/movies/list")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(MOVIE_DTO_LIST);
        // @formatter:on

		assertNotNull(responseList);
		assertNotNull(responseUpdate);
		assertNotNull(responseCreate);

		assertNotEquals(responseCreate.getTitle(), responseUpdate.getTitle());

		assertTrue(!responseList.isEmpty());
		var currentObj = responseList.stream().filter(movie -> movie.getTitle().equals(responseUpdate.getTitle())).findFirst().get();
		assertNotNull(currentObj);
		assertEquals(responseList.size() - 1, responseListAfterDelete.size());
	}

	@Test
	void shouldThrow403WhenUserNoAccess() {
		MovieDto dto = MovieDto.builder()
			.genre(Genre.DRAMA)
			.available(true)
			.title("test")
			.build();

		RegisterUserDto registerUserDto = new RegisterUserDto("arek", "pass123", "arek@notPermitted.pl");

		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .body(registerUserDto)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(200);

		var token = getToken("arek@notPermitted.pl", "pass123").getAccessToken();

		given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(dto)
        .when()
            .post("/api/v1/movies")
        .then()
            .statusCode(403);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(dto)
        .when()
            .delete("/api/v1/movies/12")
        .then()
            .statusCode(403);
        // @formatter:on
	}

	private TokenAuthDto getToken(String username, String password) {
		LoginDto loginDto = new LoginDto(username, password);

		// @formatter:off
        return given()
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
	}

}

package com.arekbednarz.controller;

import com.arekbednarz.BackendApplication;
import com.arekbednarz.dto.auth.LoginDto;
import com.arekbednarz.dto.auth.TokenAuthDto;
import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.rental.RentalDto;
import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.RentTimeEnum;
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
class RentManageControllerTest {

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	// @formatter:off
    @SuppressWarnings("serial")
    private static final Type RENTAL_DTO_LIST = new ArrayList<RentalDto>() {}.getClass().getGenericSuperclass();
    // @formatter:on

	@Test
	void shouldRentAndNextReturnMovie() {
		MovieDto dto = MovieDto.builder()
			.genre(Genre.DRAMA)
			.available(true)
			.title("test")
			.build();

		var token = getToken("noobuser@email.com", "test").getAccessToken();
		var adminToken = getToken("admin@email.com", "test").getAccessToken();

		// @formatter:off
        var responseCreate =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(dto)
            .when()
                .post("/api/v1/movies")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(MovieDto.class);

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .param("dueDate",RentTimeEnum.ONE_DAY)
            .when()
                .put("/api/v1/rental/"+responseCreate.getId()+"/rent")
            .then()
                .statusCode(204);

        List<RentalDto> responseListAfterRent =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .param("returned",false)
            .when()
                .get("/api/v1/rental/self/0/10/rentals")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(RENTAL_DTO_LIST);

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(dto)
            .when()
                .put("/api/v1/rental/"+responseCreate.getId()+"/return")
            .then()
                .statusCode(204);

        List<RentalDto> responseListAfterReturn =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .param("returned",false)
            .when()
                .get("/api/v1/rental/self/0/10/rentals")
            .then()
                .statusCode(200)
                .extract()
                .body()
                .as(RENTAL_DTO_LIST);

            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
            .when()
                .delete("/api/v1/movies/"+responseCreate.getId())
            .then()
                .statusCode(204);
        // @formatter:on

		assertNotNull(responseListAfterRent);
		assertEquals(1, responseListAfterRent.size());
		assertTrue(responseListAfterReturn.isEmpty());
	}

	@Test
	void shouldThrow403WhenUserNoAccess() {
		var token = getToken("noobuser@email.com", "test").getAccessToken();

		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .param("returned",false)
        .when()
            .get("/api/v1/rental/user/32/1/10/rentals")
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

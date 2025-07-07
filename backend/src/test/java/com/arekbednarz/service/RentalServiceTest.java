package com.arekbednarz.service;

import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.userMgmt.UserDto;
import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.Role;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.exception.MovieNotAvailableException;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.repository.MovieRepository;
import com.arekbednarz.model.repository.RentalsRepository;
import com.arekbednarz.utils.PostgresqlTestContainer;
import com.github.javafaker.Faker;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RentalServiceTest {

	@Autowired
	private IRentalService rentalService;

	@Autowired
	private RentalsRepository rentalsRepository;

	@MockitoSpyBean
	private MovieRepository movieRepository;

	@MockitoSpyBean
	@Qualifier("movieManageService")
	private IManageService movieService;

	@MockitoSpyBean
	@Qualifier("userCreationService")
	private IManageService userService;

	private final Faker FAKER = new Faker();

	@Test
	void shouldRentMovie() {
		var movieDto = getMovieDtoObject();
		var userDto = getUserDtoObject();

		Movie movieDb = movieService.create(movieDto);
		User userDb = userService.create(userDto);

		var rentDate = LocalDateTime.now();

		var rentedMoviesBefore = rentalService.getRentalsPaged(userDb.getId(), false, 0, 10);

		rentalService.rentMovie(movieDb.getId(), userDb, rentDate);

		var rentedMoviesAfter = rentalService.getRentalsPaged(userDb.getId(), false, 0, 10);

		assertNotEquals(rentedMoviesBefore, rentedMoviesAfter);
		assertEquals(rentedMoviesBefore.size() + 1, rentedMoviesAfter.size());
	}

	@Test
	void shouldReturnMovie() {
		var movieDto1 = getMovieDtoObject();
		var movieDto2 = getMovieDtoObject();
		var userDto = getUserDtoObject();

		Movie movieDb1 = movieService.create(movieDto1);
		Movie movieDb2 = movieService.create(movieDto2);
		User userDb = userService.create(userDto);

		var rentDate = LocalDateTime.now().plusDays(4L);

		var rentedMoviesBefore = rentalService.getRentalsPaged(userDb.getId(), false, 0, 10);

		rentalService.rentMovie(movieDb1.getId(), userDb, rentDate);
		rentalService.rentMovie(movieDb2.getId(), userDb, rentDate);

		var returnedMoviesBefore = rentalService.getRentalsPaged(userDb.getId(), true, 0, 10);

		rentalService.returnMovie(movieDb1.getId(), userDb);

		var returnedMoviesAfter = rentalService.getRentalsPaged(userDb.getId(), true, 0, 10);
		var rentedMoviesAfter = rentalService.getRentalsPaged(userDb.getId(), false, 0, 10);

		assertNotEquals(returnedMoviesBefore, returnedMoviesAfter);
		assertEquals(rentedMoviesBefore.size() + 1, rentedMoviesAfter.size());
		assertEquals(returnedMoviesBefore.size() + 1, returnedMoviesAfter.size());
	}

	@Test
	void shouldThrowExceptionWhenMovieIsNotAvailable() {
		Movie movie = Movie.builder()
			.id(1L)
			.available(false)
			.title(FAKER.internet().avatar())
			.genre(Genre.DRAMA)
			.build();

		when(movieRepository.findById(anyLong())).thenReturn(Optional.ofNullable(movie));

		assertThrows(MovieNotAvailableException.class, () -> rentalService.rentMovie(1L, new User(), LocalDateTime.now().plusDays(4L)));
	}

	@Test
	void shouldThrowExceptionWhenMovieNotFound() {
		assertThrows(EntityNotFoundException.class, () -> rentalService.rentMovie(99L, new User(), LocalDateTime.now().plusDays(4L)));
	}

	@Test
	void shouldThrowExceptionWhenPageOrSizeIsIncorrect() {
		assertThrows(BadRequestException.class, () -> rentalService.getRentalsPaged(21L, false, -1, -2));
	}

	private MovieDto getMovieDtoObject() {
		return MovieDto.builder()
			.title(FAKER.chuckNorris().fact())
			.genre(Genre.COMEDY)
			.available(true)
			.build();
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

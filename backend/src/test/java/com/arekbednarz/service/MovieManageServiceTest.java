package com.arekbednarz.service;

import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.movieMgmt.MovieUpdateDto;
import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.MovieUpdateAction;
import com.arekbednarz.exception.EntityCreationException;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.model.repository.MovieRepository;
import com.arekbednarz.utils.PostgresqlTestContainer;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MovieManageServiceTest {

	@MockitoSpyBean
	@Qualifier("movieManageService")
	private IManageService movieService;

	@MockitoSpyBean
	private MovieRepository movieRepository;

	private final Faker FAKER = new Faker();

	@Test
	@Order(1)
	void shouldCreateMovie() {
		var dto = getDtoObject();
		Movie movie = movieService.create(dto);
		assertNotNull(movie);
		assertNotNull(movie.getId());
		assertEquals(dto.getTitle(), movie.getTitle());
		assertEquals(dto.getGenre(), movie.getGenre());
		assertTrue(movie.available);

		assertNotNull(movieService.getOne(movie.getId()));
	}

	@Test
	@Order(2)
	void shouldCorrectlyGetMovie() {
		var movie = Movie.builder()
			.id(1L)
			.title(FAKER.chuckNorris().fact())
			.genre(Genre.DRAMA)
			.available(false)
			.build();

		when(movieRepository.findById(anyLong())).thenReturn(Optional.ofNullable(movie));

		var movieDb = ((Movie) movieService.getOne(1L));

		assertNotNull(movieDb);
		assertNotNull(movie);
		assertEquals(movie.getGenre(), movieDb.getGenre());
		assertEquals(movie.getTitle(), movieDb.getTitle());
		assertEquals(movie.isAvailable(), movieDb.isAvailable());
	}

	@Test
	@Order(3)
	void shouldCorrectlyGetAllMovies() {
		var movie = Movie.builder()
			.id(1L)
			.title(FAKER.chuckNorris().fact())
			.genre(Genre.DRAMA)
			.available(false)
			.build();

		when(movieRepository.findById(anyLong())).thenReturn(Optional.ofNullable(movie));

		List<Movie> movies = movieService.getAll();
		assertNotNull(movies);
		assertFalse(movies.isEmpty());
		assertEquals(1, movies.size());
	}

	@Test
	@Order(4)
	void shouldUpdateMovie() {
		var dto = getDtoObject();
		var movie = ((Movie) movieService.create(dto));

		var updatedName = "Dummy title";
		var updatedGenre = Genre.FANTASY;
		var updatedAv = false;

		var updateDto = new MovieUpdateDto(movie.getId(),
			Set.of(
				MovieUpdateAction.GENRE,
				MovieUpdateAction.TITLE,
				MovieUpdateAction.AVAILABILITY), updatedName, updatedGenre, updatedAv);

		movieService.update(updateDto);

		Movie updatedMovie = movieService.getOne(movie.getId());

		assertEquals(updatedMovie.getTitle(), updatedName);
		assertEquals(updatedMovie.getGenre(), updatedGenre);
		assertFalse(updatedMovie.isAvailable());
	}

	@Test
	@Order(5)
	void shouldDeleteMovie() {
		var dto = getDtoObject();
		var movie = ((Movie) movieService.create(dto));

		var movieBeforeDelete = movieService.getOne(movie.getId());

		assertNotNull(movieBeforeDelete);

		movieService.delete(movie.getId());

		assertThrows(EntityNotFoundException.class, () -> movieService.getOne(movie.getId()));
	}

	@Test
	@Order(7)
	void shouldThrowExceptionWhenUserNotExists() {
		assertThrows(EntityNotFoundException.class, () -> movieService.getOne(10L));
	}

	@Test
	@Order(8)
	void shouldThrowExceptionWhenUpdateDtoIsIncorrect() {
		var movie = Movie.builder()
			.id(1L)
			.title(FAKER.chuckNorris().fact())
			.genre(Genre.DRAMA)
			.available(false)
			.build();

		when(movieRepository.findById(anyLong())).thenReturn(Optional.ofNullable(movie));

		var updateDto = new MovieUpdateDto(movie.getId(),
			Set.of(
				MovieUpdateAction.GENRE), null, null, false);

		Exception exception = assertThrows(EntityCreationException.class, () -> movieService.update(updateDto));

		assertTrue(exception.getMessage().contains("Update source cannot be null"));
	}

	@Test
	@Order(9)
	void shouldThrowExceptionWhenObjectTypeIsIncorrect() {
		assertThrows(EntityCreationException.class, () -> movieService.getOne("test"));
	}

	private MovieDto getDtoObject() {
		return MovieDto.builder()
			.title(FAKER.chuckNorris().fact())
			.genre(Genre.COMEDY)
			.available(true)
			.build();
	}
}

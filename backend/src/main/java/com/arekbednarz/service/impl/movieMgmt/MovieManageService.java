package com.arekbednarz.service.impl.movieMgmt;

import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.movieMgmt.MovieUpdateDto;
import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.MovieUpdateAction;
import com.arekbednarz.exception.EntityCreationException;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.model.repository.MovieRepository;
import com.arekbednarz.service.IManageService;
import io.vavr.control.Option;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service("movieManageService")
public class MovieManageService implements IManageService {
	private static final Logger LOG = Logger.getLogger(MovieManageService.class);

	private final MovieRepository movieRepository;

	@Autowired
	public MovieManageService(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAll() {
		return (List<T>) movieRepository.findAll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T create(S entity) {
		var movieCreationDto = dtoParser(entity, MovieDto.class);
		try {
			return (T) movieRepository.save(createMovieObject(movieCreationDto));
		} catch (Exception e) {
			LOG.errorf("Error creating movie: %s", e.getMessage());
			throw new EntityCreationException("Movie creation failed, caused by: " + e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, S> T getOne(S entity) {
		var movieId = dtoParser(entity, Long.class);
		return (T) getMovieById(movieId);
	}

	@Override
	public <S> void delete(S id) {
		var movieId = dtoParser(id, Long.class);
		movieRepository.delete(getMovieById(movieId));
		LOG.infof("Deleted movie with id: %d", id);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public <T, S> T update(S dto) {
		var movieUpdateDto = createUpdateObject(dtoParser(dto, MovieUpdateDto.class));
		LOG.infof("Updating movie: %s", dto);
		return (T) movieRepository.save(movieUpdateDto);
	}

	private Movie createUpdateObject(final MovieUpdateDto movieUpdateDto) {
		final Long id = Option.of(movieUpdateDto.movieId())
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityCreationException("Movie id cannot be null"));

		final Set<MovieUpdateAction> movieUpdateAction = Option.of(movieUpdateDto.action())
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityCreationException("Movie update action set cannot be null or empty"));

		Movie dbMovie = getMovieById(id);

		return Movie.builder()
			.id(dbMovie.getId())
			.available(movieUpdateAction.contains(MovieUpdateAction.AVAILABILITY) ? (Boolean) defineSource(movieUpdateDto.isAvailable()) : dbMovie.isAvailable())
			.genre(movieUpdateAction.contains(MovieUpdateAction.GENRE) ? (Genre) defineSource(movieUpdateDto.genre()) : dbMovie.getGenre())
			.title(movieUpdateAction.contains(MovieUpdateAction.TITLE) ? (String) defineSource(movieUpdateDto.title()) : dbMovie.getTitle())
			.build();
	}

	private Movie createMovieObject(final MovieDto movieDto) {
		return Movie.builder()
			.title(movieDto.getTitle())
			.genre(movieDto.getGenre())
			.available(movieDto.isAvailable())
			.build();
	}

	private Movie getMovieById(final Long id) {
		return movieRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(Movie.class, "id", id.toString()));
	}
}

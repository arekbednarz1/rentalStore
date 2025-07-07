package com.arekbednarz.service.impl.rentals;

import com.arekbednarz.config.kafka.KafkaProducer;
import com.arekbednarz.dto.movieMgmt.MovieUpdateDto;
import com.arekbednarz.dto.rental.RentalDto;
import com.arekbednarz.enums.MovieUpdateAction;
import com.arekbednarz.exception.EntityNotFoundException;
import com.arekbednarz.exception.MovieNotAvailableException;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.model.entity.Rentals;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.repository.RentalsRepository;
import com.arekbednarz.service.IManageService;
import com.arekbednarz.service.IRentalService;
import com.arekbednarz.service.IStoreService;
import io.vavr.control.Option;
import jakarta.ws.rs.BadRequestException;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service("rentalsManageService")
public class RentalService implements IRentalService {
	private static final Logger LOG = Logger.getLogger(RentalService.class);
	private final IManageService movieManageService;
	private final IStoreService cacheStoreService;
	private final RentalsRepository rentalsRepository;
	private final KafkaProducer kafkaProducer;

	@Autowired
	public RentalService(
		@Qualifier("movieManageService") IManageService movieService,
		IStoreService cacheStoreService,
		RentalsRepository rentalsRepository,
		KafkaProducer kafkaProducer) {
		this.movieManageService = movieService;
		this.cacheStoreService = cacheStoreService;
		this.rentalsRepository = rentalsRepository;
		this.kafkaProducer = kafkaProducer;
	}

	public void rentMovie(final Long movieId, final User user, final LocalDateTime dueDate) {
		LOG.infof("Renting movie %d", movieId);

		Rentals rentObject = createRental(movieId, user, dueDate);

		rentalsRepository.save(rentObject);
		setMovieAvailability(movieId, false);
		kafkaProducer.scheduleReminder(rentObject);
	}

	public void returnMovie(final Long movieId, final User user) {
		LOG.infof("Returning movie %d", movieId);

		Rentals returnObject = createReturn(movieId, user);

		rentalsRepository.save(returnObject);
		setMovieAvailability(movieId, true);
		cacheStoreService.remove(returnObject.getId());
	}

	public List<RentalDto> getRentalsPaged(final Long userId, final boolean returned, final int page, final int size) {
		PageRequest paged = getPageRequest(page, size);
		return rentalsRepository
			.findRentalDtosByUserIdAndReturnedStatus(userId, returned, paged)
			.getContent();
	}

	private void setMovieAvailability(final Long movieId, final boolean isAvailable) {
		movieManageService.update(
			new MovieUpdateDto(movieId, Set.of(MovieUpdateAction.AVAILABILITY), null, null, isAvailable));
	}

	private Rentals createRental(final Long movieId, final User user, final LocalDateTime dueDate) {
		Movie movie = movieManageService.getOne(movieId);

		checkAvailability(movie);

		return Rentals.builder()
			.rentedAt(LocalDateTime.now())
			.movie(movie)
			.user(user)
			.dueDate(dueDate)
			.build();
	}

	private Rentals createReturn(final Long movieId, final User user) {
		Rentals rental = Option.of(rentalsRepository.findUserActiveMovieRental(user, movieId))
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityNotFoundException(Rentals.class, "movieId", movieId.toString()));

		rental.setReturnedAt(LocalDateTime.now());
		return rental;
	}

	private void checkAvailability(final Movie movie) {
		if (!movie.isAvailable()) {
			throw new MovieNotAvailableException(movie.getId());
		}
	}

	private PageRequest getPageRequest(final int page, final int size) {
		if (page < 0 || size <= 0)
			throw new BadRequestException("Page index must not be less than zero and page size must be greater than zero");
		return PageRequest.of(page, size, Sort.by("rentedAt").descending());
	}
}

package com.arekbednarz.controller;


import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.dto.movieMgmt.MovieUpdateDto;
import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.MovieUpdateAction;
import com.arekbednarz.mapper.MovieMapper;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.service.IManageService;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieManageController {
	private final IManageService movieManageService;
	private final MovieMapper movieMapper;

	@Autowired
	public MovieManageController(
			@Qualifier("movieManageService") IManageService movieManageService,
			MovieMapper movieMapper) {
		this.movieManageService = movieManageService;
		this.movieMapper = movieMapper;
	}

	@PutMapping(produces = "application/json")
	@PreAuthorize("hasAuthority('movie:manage')")
	public ResponseEntity updateMovie(
		@RequestParam(value = "id") final Long id,
		@RequestParam(value = "title") final String title,
		@RequestParam(value = "genre") final Genre genre,
		@RequestParam(value = "status") final boolean status) {

		if (StringUtils.isBlank(title)) {
			throw new BadRequestException("Title cannot be empty");
		}

		return ResponseEntity.ok().body(
			movieMapper.toMovieDto(movieManageService
				.update(new MovieUpdateDto(id, Set.of(MovieUpdateAction.AVAILABILITY,MovieUpdateAction.GENRE,MovieUpdateAction.TITLE), title, genre, status))));
	}

	@DeleteMapping(path = "/{id}", produces = "application/json")
	@PreAuthorize("hasAuthority('movie:manage')")
	public ResponseEntity deleteMovie(
		@PathVariable(value = "id") final Long id) {
		movieManageService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(produces = "application/json")
	@PreAuthorize("hasAuthority('movie:manage')")
	public ResponseEntity createMovie(@RequestBody final MovieDto dto) {
		return ResponseEntity.ok().body(
				movieManageService.create(dto));
	}

	@GetMapping(path = "/list", produces = "application/json")
	@PreAuthorize("hasAuthority('movie:list')")
	public ResponseEntity getAllMovies() {
		return ResponseEntity.ok().body(
			movieManageService.getAll()
				.stream()
				.map(movie -> movieMapper.toMovieDto((Movie) movie))
				.toList());
	}
}

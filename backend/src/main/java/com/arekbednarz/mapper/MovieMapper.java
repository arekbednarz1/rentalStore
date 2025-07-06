package com.arekbednarz.mapper;

import com.arekbednarz.dto.movieMgmt.MovieDto;
import com.arekbednarz.model.entity.Movie;
import org.mapstruct.Mapper;


@Mapper
public interface MovieMapper {
	MovieDto toMovieDto(Movie user);
}

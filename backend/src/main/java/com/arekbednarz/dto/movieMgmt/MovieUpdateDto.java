package com.arekbednarz.dto.movieMgmt;

import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.MovieUpdateAction;

import java.util.Set;


public record MovieUpdateDto(Long movieId, Set<MovieUpdateAction> action, String title, Genre genre, boolean isAvailable) {
}

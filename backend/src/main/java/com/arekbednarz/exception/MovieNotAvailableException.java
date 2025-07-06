package com.arekbednarz.exception;

import jakarta.ws.rs.NotFoundException;


@SuppressWarnings("serial")
public class MovieNotAvailableException extends NotFoundException {
	public MovieNotAvailableException(final Long id) {
		super(String.format("Movie with id %d is not available", id));
	}
}

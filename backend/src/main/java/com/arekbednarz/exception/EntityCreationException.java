package com.arekbednarz.exception;

import jakarta.ws.rs.BadRequestException;


@SuppressWarnings("serial")
public class EntityCreationException extends BadRequestException {
	public EntityCreationException(final String message) {
		super(message);
	}
}

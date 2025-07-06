package com.arekbednarz.exception;

import jakarta.ws.rs.NotFoundException;


@SuppressWarnings("serial")
public class EntityNotFoundException extends NotFoundException {
	public EntityNotFoundException(final Class type, final String fieldName, final String fieldValue) {
		super(String.format("Entity of type %s with %s : %s not found", type.getName(), fieldName, fieldValue));
	}
}

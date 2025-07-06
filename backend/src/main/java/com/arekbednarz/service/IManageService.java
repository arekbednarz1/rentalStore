package com.arekbednarz.service;

import com.arekbednarz.exception.EntityCreationException;
import io.vavr.control.Option;

import java.util.List;
import java.util.Objects;


public interface IManageService {
	<T> List<T> getAll();

	<T, S> T create(S entity);

	<T, S> T getOne(S entity);

	<S> void delete(S entity);

	<T, S> T update(S entity);

	default <T, S> S dtoParser(final T vanillaDto, final Class<S> expectedType) {
		if (vanillaDto == null) {
			throw new EntityCreationException("DTO object cannot be null.");
		}
		if (expectedType.isInstance(vanillaDto)) {
			return expectedType.cast(vanillaDto);
		}
		throw new EntityCreationException("Cannot parse DTO object; expected: " + expectedType.getSimpleName() + ", current: " + vanillaDto.getClass().getSimpleName());
	}

	default <T> Object defineSource(T source) {
		return Option.of(source)
			.filter(Objects::nonNull)
			.getOrElseThrow(() -> new EntityCreationException("Update source cannot be null"));
	}
}

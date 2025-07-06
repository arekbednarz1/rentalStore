package com.arekbednarz.service;

import com.arekbednarz.dto.rental.RentalDto;
import com.arekbednarz.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface IRentalService {
    void rentMovie(final Long movieId, final User user, final LocalDateTime dueDate);
    void returnMovie(final Long movieId,final User user);
    List<RentalDto> getRentalsPaged(final Long userId, final boolean returned, final int page, final int size);
}

package com.arekbednarz.model.repository;

import com.arekbednarz.dto.rental.RentalDto;
import com.arekbednarz.model.entity.Movie;
import com.arekbednarz.model.entity.Rentals;
import com.arekbednarz.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RentalsRepository extends JpaRepository<Rentals, Long> {
    @Query("SELECT r FROM Rentals r WHERE r.returnedAt IS null AND r.user =:user AND r.movie.id =:movieId")
    Rentals findUserActiveMovieRental(@Param("user") User user, @Param("movieId") Long movieId);

    @Query("""
    SELECT new com.arekbednarz.dto.rental.RentalDto(
        m.id, u.email,m.title,m.genre,r.rentedAt,r.returnedAt
    )
    FROM Rentals r
    JOIN r.user u
    JOIN r.movie m
    WHERE u.id = :userId
      AND (
        :returned IS NULL OR
        (:returned = TRUE AND r.returnedAt IS NOT NULL) OR
        (:returned = FALSE AND r.returnedAt IS NULL)
      )
""")
    Page<RentalDto> findRentalDtosByUserIdAndReturnedStatus(
            @Param("userId") Long userId,
            @Param("returned") Boolean returned,
            Pageable pageable
    );

}

package com.arekbednarz.dto.rental;

import com.arekbednarz.enums.Genre;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalDto {

    @JsonProperty("movieId")
    private Long movieId;

    @JsonProperty("userEmail")
    private String userEmail;

    @JsonProperty("movieTitle")
    private String movieTitle;

    @JsonProperty("genre")
    private Genre genre;

    @JsonProperty("rentedAt")
    private LocalDateTime rentedAt;

    @JsonProperty("returnedAt")
    private LocalDateTime returnedAt;

}

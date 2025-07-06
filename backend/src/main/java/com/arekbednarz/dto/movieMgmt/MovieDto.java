package com.arekbednarz.dto.movieMgmt;

import com.arekbednarz.enums.Genre;
import com.arekbednarz.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class MovieDto {

	@JsonProperty("id")
	private Long id;

	@NotNull
	@NotEmpty
	@JsonProperty("title")
	private String title;

	@NotNull
	@JsonProperty("genre")
	private Genre genre;

	@NotNull
	@JsonProperty("available")
	private boolean available;

}

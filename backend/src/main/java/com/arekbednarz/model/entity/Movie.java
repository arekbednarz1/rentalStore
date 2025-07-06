package com.arekbednarz.model.entity;

import com.arekbednarz.enums.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movies", uniqueConstraints = { @UniqueConstraint(columnNames = "id") })
public @Data class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Long id;

	@Column(name = "title", nullable = false)
	public String title;

	@Column(name = "genre")
	@Enumerated(EnumType.STRING)
	public Genre genre;

	@Column(name = "available")
	public boolean available;

}

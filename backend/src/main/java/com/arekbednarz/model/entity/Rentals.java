package com.arekbednarz.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
	name = "rentals",
	uniqueConstraints = { @UniqueConstraint(columnNames = "id") },
	indexes = {
		@Index(name = "idx_rentals_user_id", columnList = "user_id"),
		@Index(name = "idx_rentals_returned_at", columnList = "returned_at"),
		@Index(name = "idx_rentals_rented_at", columnList = "rented_at"),
		@Index(name = "idx_rentals_user_returned", columnList = "user_id, returned_at")
	})
public @Data class Rentals {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;

	@Column(name = "rented_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime rentedAt;

	@Column(name = "due_date", nullable = false)
	private LocalDateTime dueDate;

	@Column(name = "returned_at")
	private LocalDateTime returnedAt;

}

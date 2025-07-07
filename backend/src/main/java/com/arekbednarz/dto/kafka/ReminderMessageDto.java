package com.arekbednarz.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public @Data class ReminderMessageDto {
	private Long rentalId;
	private String userEmail;
	private String movieTitle;
	private LocalDateTime dueDate;
}

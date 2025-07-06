package com.arekbednarz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
public enum RentTimeEnum {
	ONE_DAY(LocalDateTime.now().plusDays(1)),
	ONE_WEEK(LocalDateTime.now().plusWeeks(1)),;

	@Getter
	private final LocalDateTime dateTime;
}

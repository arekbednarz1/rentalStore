package com.arekbednarz.service;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import java.util.List;


public interface IStoreService {
	void add(ReminderMessageDto message);

	void remove(Long rentalId);

	List<ReminderMessageDto> getAll();
}

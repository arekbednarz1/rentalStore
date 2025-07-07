package com.arekbednarz.config.kafka;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.arekbednarz.service.IStoreService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class KafkaConsumer {
	private final IStoreService storeService;

	@KafkaListener(topics = "rental-reminders", groupId = "reminder-group")
	public void handleReminder(ReminderMessageDto message) {
		storeService.add(message);
	}
}

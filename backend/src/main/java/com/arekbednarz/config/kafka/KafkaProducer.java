package com.arekbednarz.config.kafka;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.arekbednarz.model.entity.Rentals;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@AllArgsConstructor
public class KafkaProducer {
	private static final Logger LOG = Logger.getLogger(KafkaProducer.class);

	private final KafkaTemplate<String, ReminderMessageDto> kafkaTemplate;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void scheduleReminder(Rentals rental) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime reminderTime = rental.getDueDate().minusHours(24);

		LOG.info("Scheduling reminder for " + reminderTime);
		if (now.isAfter(reminderTime)) {
			LOG.info("Reminder time is outdated sending reminder immediately");
			sendReminder(rental);
			return;
		}

		Duration delay = Duration.between(now, reminderTime);

		scheduler.schedule(() -> sendReminder(rental), delay.toMillis(), TimeUnit.MILLISECONDS);
	}

	private void sendReminder(Rentals rental) {
		ReminderMessageDto message = ReminderMessageDto.builder()
			.rentalId(rental.getId())
			.userEmail(rental.getUser().getEmail())
			.movieTitle(rental.getMovie().getTitle())
			.dueDate(rental.getDueDate())
			.build();

		kafkaTemplate.send("rental-reminders", rental.getId().toString(), message);
	}
}

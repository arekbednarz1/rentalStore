package com.arekbednarz.service;

import com.arekbednarz.config.kafka.KafkaProducer;
import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.arekbednarz.model.entity.Rentals;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.model.entity.Movie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

    @Test
    void shouldSendReminderImmediatelyIfLessThan24Hours() {
        KafkaTemplate<String, ReminderMessageDto> kafkaTemplate = mock();

        KafkaProducer producer = new KafkaProducer(kafkaTemplate);

        Rentals rental = mock();
        when(rental.getDueDate()).thenReturn(LocalDateTime.now().plusHours(12));
        when(rental.getId()).thenReturn(42L);

        User user = mock();
        when(user.getEmail()).thenReturn("test@test.com");
        when(rental.getUser()).thenReturn(user);

        Movie movie = mock();
        when(movie.getTitle()).thenReturn("TEST");
        when(rental.getMovie()).thenReturn(movie);

        producer.scheduleReminder(rental);

        ArgumentCaptor<ReminderMessageDto> captor = ArgumentCaptor.forClass(ReminderMessageDto.class);
        verify(kafkaTemplate).send(eq("rental-reminders"), eq("42"), captor.capture());

        ReminderMessageDto sent = captor.getValue();
        assertEquals("TEST", sent.getMovieTitle());
        assertEquals("test@test.com", sent.getUserEmail());
    }
}

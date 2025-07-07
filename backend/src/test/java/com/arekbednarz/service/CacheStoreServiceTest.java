package com.arekbednarz.service;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.arekbednarz.service.impl.cache.CacheStoreService;
import com.arekbednarz.utils.PostgresqlTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ContextConfiguration;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ContextConfiguration(initializers = { PostgresqlTestContainer.class })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CacheStoreServiceTest {

	private CacheStoreService cacheStoreService;
	private CaffeineCache caffeineCache;
	private ConcurrentHashMap<Object, Object> internalMap;

	@BeforeEach
	void setUp() {
		CacheManager cacheManager = mock();
		caffeineCache = mock();
		cacheStoreService = new CacheStoreService(cacheManager);

		internalMap = new ConcurrentHashMap<>();
		com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
			spy(com.github.benmanes.caffeine.cache.Caffeine.newBuilder().build());
		when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
		lenient().when(nativeCache.asMap()).thenReturn(internalMap);
		lenient().when(cacheManager.getCache("reminders")).thenReturn(caffeineCache);
	}

	@Test
	void shouldAddReminderToCache() {
		ReminderMessageDto message = ReminderMessageDto.builder()
			.rentalId(1L)
			.movieTitle("TEST1")
			.userEmail("some@noob.com")
			.dueDate(java.time.LocalDateTime.now())
			.build();

		cacheStoreService.add(message);

		verify(caffeineCache).put(1L, message);
	}

	@Test
	void shouldRemoveReminderFromCache() {
		cacheStoreService.remove(42L);
		verify(caffeineCache).evict(42L);
	}

	@Test
	void shouldReturnAllRemindersFromCache() {
		ReminderMessageDto msg = ReminderMessageDto.builder()
			.rentalId(1L)
			.movieTitle("TEST")
			.userEmail("test@email.com")
			.dueDate(java.time.LocalDateTime.now())
			.build();

		internalMap.put(1L, msg);

		var results = cacheStoreService.getAll();

		assertEquals(1, results.size());
		assertEquals("TEST", results.get(0).getMovieTitle());
	}
}

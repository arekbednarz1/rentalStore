package com.arekbednarz.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
			.maximumSize(1000);

		CaffeineCacheManager manager = new CaffeineCacheManager("reminders");
		manager.setCaffeine(caffeine);
		return manager;
	}
}

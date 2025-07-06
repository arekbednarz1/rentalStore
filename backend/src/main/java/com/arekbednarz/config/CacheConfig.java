package com.arekbednarz.config;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfter(new Expiry<>() {
                    @Override
                    public long expireAfterCreate(Object key, Object rawValue, long currentTime) {
                        if (!(rawValue instanceof ReminderMessageDto value)) return 0;

                        LocalDateTime expiryTime = value.getDueDate().minusHours(24);
                        boolean expired = LocalDateTime.now().isAfter(expiryTime);

                        if (expired) return 0;

                        return Duration.between(LocalDateTime.now(), expiryTime).toMillis();
                    }

                    @Override
                    public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                });

        CaffeineCacheManager manager = new CaffeineCacheManager("reminders");
        manager.setCaffeine(caffeine);
        return manager;
    }
}


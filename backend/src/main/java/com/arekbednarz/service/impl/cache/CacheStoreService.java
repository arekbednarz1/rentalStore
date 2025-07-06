package com.arekbednarz.service.impl.cache;

import com.arekbednarz.dto.kafka.ReminderMessageDto;
import com.arekbednarz.service.IStoreService;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CacheStoreService implements IStoreService {
    private final CacheManager cacheManager;

    public void add(ReminderMessageDto message) {
        cacheManager.getCache("reminders").put(message.getRentalId(), message);
    }

    public void remove(Long rentalId) {
        cacheManager.getCache("reminders").evict(rentalId);
    }

    public List<ReminderMessageDto> getAll() {
        Cache cache = cacheManager.getCache("reminders");

        if (cache instanceof CaffeineCache caffeineCache) {
            return caffeineCache.getNativeCache()
                    .asMap()
                    .values()
                    .stream()
                    .filter(obj -> obj instanceof ReminderMessageDto)
                    .map(obj -> (ReminderMessageDto) obj)
                    .toList();
        }
        return List.of();
    }

}

package tn.esprit.mentorservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.esprit.mentorservice.common.ApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MentorRateLimiter {

    private final long minIntervalMs;
    private final Map<Long, Long> lastAskMs = new ConcurrentHashMap<>();

    public MentorRateLimiter(@Value("${mentor.ask-rate-min-interval-ms:3000}") long minIntervalMs) {
        this.minIntervalMs = minIntervalMs;
    }

    public void verifyAskAllowed(long userId) {
        if (minIntervalMs <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        synchronized (lastAskMs) {
            Long last = lastAskMs.get(userId);
            if (last != null && now - last < minIntervalMs) {
                throw new ApiException(429, "Please wait a moment before asking again.");
            }
            lastAskMs.put(userId, now);
        }
    }
}

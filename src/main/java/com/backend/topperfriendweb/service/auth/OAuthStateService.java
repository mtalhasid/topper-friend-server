package com.backend.topperfriendweb.service.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuthStateService {
    private static final long STATE_TTL_SECONDS = 300; // 5 minutes
    private final Map<String, Long> stateStore = new ConcurrentHashMap<>();

    // Optionally called when you generate an OAuth URL to store state
    public void storeState(String state) {
        if (state != null && !state.isBlank()) {
            stateStore.put(state, Instant.now().getEpochSecond());
        }
    }

    // Validate and consume the state exactly once
    public boolean validateAndConsume(String state) {
        if (state == null || state.isBlank()) return false;
        Long createdAt = stateStore.remove(state);
        if (createdAt == null) return false; // not found
        long age = Instant.now().getEpochSecond() - createdAt;
        return age <= STATE_TTL_SECONDS;
    }
}

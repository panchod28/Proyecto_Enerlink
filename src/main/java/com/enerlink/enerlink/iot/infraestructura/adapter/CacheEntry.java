package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.time.Instant;
import java.time.Duration;

public class CacheEntry<T> {
    private final T value;
    private final Instant expiresAt;
    private int hitCount;

    public CacheEntry(T value, Duration ttl) {
        this.value = value;
        this.expiresAt = Instant.now().plus(ttl);
        this.hitCount = 0;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public T getValue() {
        hitCount++;
        return value;
    }

    public int getHitCount() {
        return hitCount;
    }

    public T getValueIfValid() {
        if (isExpired()) {
            return null;
        }
        return getValue();
    }
}
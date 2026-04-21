package com.enerlink.enerlink.iot.infraestructura.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CachingIoTDataProxy implements IoTDataPort {

    private static final Logger logger = LoggerFactory.getLogger(CachingIoTDataProxy.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final IoTDataPort delegate;
    private final Map<String, CacheEntry<IoTDeviceData>> deviceCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<IoTDeviceData>>> listCache = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Duration ttl;

    public CachingIoTDataProxy(IoTDataPort delegate) {
        this(delegate, DEFAULT_TTL);
    }

    public CachingIoTDataProxy(IoTDataPort delegate, Duration ttl) {
        this.delegate = delegate;
        this.ttl = ttl;
    }

    private ReentrantLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        String cacheKey = "device:" + deviceId;
        
        CacheEntry<IoTDeviceData> entry = deviceCache.get(cacheKey);
        if (entry != null) {
            IoTDeviceData cached = entry.getValueIfValid();
            if (cached != null) {
                logger.debug("Cache HIT for device: {} (hits: {})", deviceId, entry.getHitCount());
                return cached;
            }
            deviceCache.remove(cacheKey);
        }

        ReentrantLock lock = getLock(cacheKey);
        lock.lock();
        try {
            entry = deviceCache.get(cacheKey);
            if (entry != null) {
                IoTDeviceData cached = entry.getValueIfValid();
                if (cached != null) {
                    return cached;
                }
            }

            logger.info("Cache MISS for device: {}, fetching from provider", deviceId);
            IoTDeviceData result = delegate.fetchDeviceData(deviceId);
            
            if (result != null) {
                deviceCache.put(cacheKey, new CacheEntry<>(result, ttl));
            }
            
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        String cacheKey = "all";
        
        CacheEntry<List<IoTDeviceData>> entry = listCache.get(cacheKey);
        if (entry != null) {
            List<IoTDeviceData> cached = entry.getValueIfValid();
            if (cached != null) {
                logger.debug("Cache HIT for all devices (hits: {})", entry.getHitCount());
                return cached;
            }
            listCache.remove(cacheKey);
        }

        ReentrantLock lock = getLock(cacheKey);
        lock.lock();
        try {
            entry = listCache.get(cacheKey);
            if (entry != null) {
                List<IoTDeviceData> cached = entry.getValueIfValid();
                if (cached != null) {
                    return cached;
                }
            }

            logger.info("Cache MISS for all devices, fetching from provider");
            List<IoTDeviceData> result = delegate.fetchAllDevices();
            
            if (result != null && !result.isEmpty()) {
                listCache.put(cacheKey, new CacheEntry<>(result, ttl));
            }
            
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        String cacheKey = "location:" + location;
        
        CacheEntry<List<IoTDeviceData>> entry = listCache.get(cacheKey);
        if (entry != null) {
            List<IoTDeviceData> cached = entry.getValueIfValid();
            if (cached != null) {
                logger.debug("Cache HIT for location: {} (hits: {})", location, entry.getHitCount());
                return cached;
            }
            listCache.remove(cacheKey);
        }

        ReentrantLock lock = getLock(cacheKey);
        lock.lock();
        try {
            entry = listCache.get(cacheKey);
            if (entry != null) {
                List<IoTDeviceData> cached = entry.getValueIfValid();
                if (cached != null) {
                    return cached;
                }
            }

            logger.info("Cache MISS for location: {}, fetching from provider", location);
            List<IoTDeviceData> result = delegate.fetchDevicesByLocation(location);
            
            if (result != null && !result.isEmpty()) {
                listCache.put(cacheKey, new CacheEntry<>(result, ttl));
            }
            
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        String cacheKey = "type:" + deviceType;
        
        CacheEntry<List<IoTDeviceData>> entry = listCache.get(cacheKey);
        if (entry != null) {
            List<IoTDeviceData> cached = entry.getValueIfValid();
            if (cached != null) {
                logger.debug("Cache HIT for type: {} (hits: {})", deviceType, entry.getHitCount());
                return cached;
            }
            listCache.remove(cacheKey);
        }

        ReentrantLock lock = getLock(cacheKey);
        lock.lock();
        try {
            entry = listCache.get(cacheKey);
            if (entry != null) {
                List<IoTDeviceData> cached = entry.getValueIfValid();
                if (cached != null) {
                    return cached;
                }
            }

            logger.info("Cache MISS for type: {}, fetching from provider", deviceType);
            List<IoTDeviceData> result = delegate.fetchDevicesByType(deviceType);
            
            if (result != null && !result.isEmpty()) {
                listCache.put(cacheKey, new CacheEntry<>(result, ttl));
            }
            
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void invalidateDevice(String deviceId) {
        String cacheKey = "device:" + deviceId;
        deviceCache.remove(cacheKey);
        logger.info("Cache invalidated for device: {}", deviceId);
    }

    public void invalidateAll() {
        deviceCache.clear();
        listCache.clear();
        logger.info("All caches invalidated");
    }

    public int getDeviceCacheSize() {
        return deviceCache.size();
    }

    public int getListCacheSize() {
        return listCache.size();
    }
}
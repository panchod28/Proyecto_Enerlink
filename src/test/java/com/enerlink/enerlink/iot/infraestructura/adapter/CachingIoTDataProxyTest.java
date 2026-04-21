package com.enerlink.enerlink.iot.infraestructura.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

@ExtendWith(MockitoExtension.class)
class CachingIoTDataProxyTest {

    @Mock
    private IoTDataPort delegate;

    private CachingIoTDataProxy proxy;
    private IoTDeviceData testDevice;

    @BeforeEach
    void setUp() {
        proxy = new CachingIoTDataProxy(delegate, Duration.ofSeconds(0));
        testDevice = new IoTDeviceData();
        testDevice.setDeviceId("device-001");
        testDevice.setDeviceType(DeviceType.SENSOR);
    }

    @Test
    @DisplayName("Cache miss on fetchDeviceData — delegate is called once")
    void fetchDeviceData_cacheMiss_delegateIsCalled() {
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);

        IoTDeviceData result = proxy.fetchDeviceData("device-001");

        assertNotNull(result);
        assertEquals("device-001", result.getDeviceId());
        verify(delegate, times(1)).fetchDeviceData("device-001");
    }

    @Test
    @DisplayName("Cache hit on fetchDeviceData — delegate is NOT called a second time")
    void fetchDeviceData_cacheHit_delegateNotCalledAgain() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);

        freshProxy.fetchDeviceData("device-001");
        IoTDeviceData result = freshProxy.fetchDeviceData("device-001");

        assertNotNull(result);
        verify(delegate, times(1)).fetchDeviceData("device-001");
    }

    @Test
    @DisplayName("Cache miss on fetchAllDevices — delegate called")
    void fetchAllDevices_cacheMiss_delegateIsCalled() {
        List<IoTDeviceData> devices = List.of(testDevice);
        when(delegate.fetchAllDevices()).thenReturn(devices);

        List<IoTDeviceData> result = proxy.fetchAllDevices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(delegate, times(1)).fetchAllDevices();
    }

    @Test
    @DisplayName("Cache hit on fetchAllDevices — delegate not called again")
    void fetchAllDevices_cacheHit_delegateNotCalledAgain() {
        List<IoTDeviceData> devices = List.of(testDevice);
        when(delegate.fetchAllDevices()).thenReturn(devices);

        proxy.fetchAllDevices();
        proxy.fetchAllDevices();

        verify(delegate, times(1)).fetchAllDevices();
    }

    @Test
    @DisplayName("Cache expires after TTL — delegate called again after expiration (use a 0-second TTL)")
    void fetchDeviceData_afterTTLExpired_delegateCalledAgain() {
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);

        proxy.fetchDeviceData("device-001");
        verify(delegate, times(1)).fetchDeviceData("device-001");
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        proxy.fetchDeviceData("device-001");
        verify(delegate, times(2)).fetchDeviceData("device-001");
    }

    @Test
    @DisplayName("fetchDeviceData returns null — null is NOT cached")
    void fetchDeviceData_returnsNull_nullNotCached() {
        when(delegate.fetchDeviceData("device-001")).thenReturn(null);

        IoTDeviceData result1 = proxy.fetchDeviceData("device-001");
        IoTDeviceData result2 = proxy.fetchDeviceData("device-001");

        assertNull(result1);
        assertNull(result2);
        verify(delegate, times(2)).fetchDeviceData("device-001");
    }

    @Test
    @DisplayName("fetchAllDevices returns empty list — empty list is NOT cached")
    void fetchAllDevices_returnsEmptyList_emptyListNotCached() {
        when(delegate.fetchAllDevices()).thenReturn(Collections.emptyList());

        List<IoTDeviceData> result1 = proxy.fetchAllDevices();
        List<IoTDeviceData> result2 = proxy.fetchAllDevices();

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        verify(delegate, times(2)).fetchAllDevices();
    }

    @Test
    @DisplayName("invalidateDevice removes only that device from cache")
    void invalidateDevice_removesOnlyThatDevice() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        IoTDeviceData device2 = new IoTDeviceData();
        device2.setDeviceId("device-002");

        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);
        when(delegate.fetchDeviceData("device-002")).thenReturn(device2);

        freshProxy.fetchDeviceData("device-001");
        freshProxy.fetchDeviceData("device-002");

        freshProxy.invalidateDevice("device-001");

        verify(delegate, times(1)).fetchDeviceData("device-001");
        verify(delegate, times(1)).fetchDeviceData("device-002");
    }

    @Test
    @DisplayName("invalidateAll clears both device and list caches")
    void invalidateAll_clearsBothCaches() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);
        when(delegate.fetchAllDevices()).thenReturn(List.of(testDevice));

        freshProxy.fetchDeviceData("device-001");
        freshProxy.fetchAllDevices();

        freshProxy.invalidateAll();

        freshProxy.fetchDeviceData("device-001");
        freshProxy.fetchAllDevices();

        verify(delegate, times(2)).fetchDeviceData("device-001");
        verify(delegate, times(2)).fetchAllDevices();
    }

    @Test
    @DisplayName("Two different device IDs are cached independently")
    void fetchDeviceData_twoDifferentIds_cachedIndependently() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        IoTDeviceData device1 = new IoTDeviceData();
        device1.setDeviceId("device-001");
        IoTDeviceData device2 = new IoTDeviceData();
        device2.setDeviceId("device-002");

        when(delegate.fetchDeviceData("device-001")).thenReturn(device1);
        when(delegate.fetchDeviceData("device-002")).thenReturn(device2);

        freshProxy.fetchDeviceData("device-001");
        freshProxy.fetchDeviceData("device-002");
        IoTDeviceData result1 = freshProxy.fetchDeviceData("device-001");

        assertEquals("device-001", result1.getDeviceId());
        verify(delegate, times(1)).fetchDeviceData("device-001");
        verify(delegate, times(1)).fetchDeviceData("device-002");
    }

    @Test
    @DisplayName("Same location key reuses cached result on second call")
    void fetchDevicesByLocation_cacheHit_reusesCachedResult() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        testDevice.setLocation("home");
        List<IoTDeviceData> devices = List.of(testDevice);
        when(delegate.fetchDevicesByLocation("home")).thenReturn(devices);

        freshProxy.fetchDevicesByLocation("home");
        freshProxy.fetchDevicesByLocation("home");

        verify(delegate, times(1)).fetchDevicesByLocation("home");
    }

    @Test
    @DisplayName("Same device type key reuses cached result on second call")
    void fetchDevicesByType_cacheHit_reusesCachedResult() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        List<IoTDeviceData> devices = List.of(testDevice);
        when(delegate.fetchDevicesByType("SENSOR")).thenReturn(devices);

        freshProxy.fetchDevicesByType("SENSOR");
        freshProxy.fetchDevicesByType("SENSOR");

        verify(delegate, times(1)).fetchDevicesByType("SENSOR");
    }

    @Test
    @DisplayName("Proxy correctly returns the value from delegate on first call")
    void fetchDeviceData_firstCall_returnsDelegateValue() {
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);

        IoTDeviceData result = proxy.fetchDeviceData("device-001");

        assertSame(testDevice, result);
        verify(delegate).fetchDeviceData("device-001");
    }

    @Test
    @DisplayName("getDeviceCacheSize reflects correct count after multiple fetches")
    void cacheSizes_trackedCorrectly() {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        IoTDeviceData device2 = new IoTDeviceData();
        device2.setDeviceId("device-002");

        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);
        when(delegate.fetchDeviceData("device-002")).thenReturn(device2);
        when(delegate.fetchAllDevices()).thenReturn(List.of(testDevice));

        assertEquals(0, freshProxy.getDeviceCacheSize());
        assertEquals(0, freshProxy.getListCacheSize());

        freshProxy.fetchDeviceData("device-001");

        assertEquals(1, freshProxy.getDeviceCacheSize());
        assertEquals(0, freshProxy.getListCacheSize());

        freshProxy.fetchDeviceData("device-002");

        assertEquals(2, freshProxy.getDeviceCacheSize());

        freshProxy.fetchAllDevices();

        assertEquals(2, freshProxy.getDeviceCacheSize());
        assertEquals(1, freshProxy.getListCacheSize());
    }

    @Test
    @DisplayName("Concurrent behavior: two threads fetch same device — delegate called only once (use CountDownLatch)")
    void fetchDeviceData_concurrentThreads_delegateCalledOnce() throws InterruptedException {
        CachingIoTDataProxy freshProxy = new CachingIoTDataProxy(delegate, Duration.ofHours(1));
        
        when(delegate.fetchDeviceData("device-001")).thenReturn(testDevice);

        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger resultCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    IoTDeviceData result = freshProxy.fetchDeviceData("device-001");
                    if (result != null) {
                        resultCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        verify(delegate, times(1)).fetchDeviceData("device-001");
        assertEquals(2, resultCount.get());
    }
}
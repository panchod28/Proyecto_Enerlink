package com.enerlink.enerlink.iot.dominio.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.modelo.IoTProcessor;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bridge Pattern Tests - IoTProcessor")
class IoTProcessorBridgeTest {

    @Mock
    private IoTDataPort mockDataPort;

    @Nested
    @DisplayName("SimpleIoTProcessor Tests")
    class SimpleIoTProcessorTests {

        private IoTProcessor processor;

        @BeforeEach
        void setUp() {
            processor = new SimpleIoTProcessor(mockDataPort);
        }

        @Test
        @DisplayName("Should return data as-is when processing device")
        void processDeviceData_shouldReturnDataAsIs() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "online");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNotNull(result);
            assertEquals("DEV-001", result.getDeviceId());
            assertEquals("Smart Meter", result.getDeviceName());
            verify(mockDataPort).fetchDeviceData("DEV-001");
        }

        @Test
        @DisplayName("Should return null when data is null")
        void processDeviceData_withNullData_shouldReturnNull() {
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(null);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNull(result);
        }

        @Test
        @DisplayName("Should process all devices")
        void processAllDevices_shouldReturnAllDevices() {
            List<IoTDeviceData> devices = List.of(
                createTestDevice("DEV-001", "Meter 1", "online"),
                createTestDevice("DEV-002", "Meter 2", "online"),
                createTestDevice("DEV-003", "Meter 3", "offline")
            );
            when(mockDataPort.fetchAllDevices()).thenReturn(devices);

            List<IoTDeviceData> results = processor.processAllDevices();

            assertEquals(3, results.size());
            verify(mockDataPort).fetchAllDevices();
        }

        @Test
        @DisplayName("Should process devices by location")
        void processDevicesByLocation_shouldFilterByLocation() {
            List<IoTDeviceData> devices = List.of(
                createTestDeviceWithLocation("DEV-001", "Meter", "Living Room", "online")
            );
            when(mockDataPort.fetchDevicesByLocation("Living Room")).thenReturn(devices);

            List<IoTDeviceData> results = processor.processDevicesByLocation("Living Room");

            assertEquals(1, results.size());
            assertEquals("Living Room", results.get(0).getLocation());
        }

        @Test
        @DisplayName("Should process devices by type")
        void processDevicesByType_shouldFilterByType() {
            List<IoTDeviceData> devices = List.of(
                createTestDeviceWithType("DEV-001", "Meter", DeviceType.SMART_METER, "online")
            );
            when(mockDataPort.fetchDevicesByType("smart_meter")).thenReturn(devices);

            List<IoTDeviceData> results = processor.processDevicesByType("smart_meter");

            assertEquals(1, results.size());
            assertEquals(DeviceType.SMART_METER, results.get(0).getDeviceType());
        }

        @Test
        @DisplayName("Should handle empty device list")
        void processAllDevices_withEmptyList_shouldReturnEmpty() {
            when(mockDataPort.fetchAllDevices()).thenReturn(List.of());

            List<IoTDeviceData> results = processor.processAllDevices();

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("FilteringIoTProcessor Tests")
    class FilteringIoTProcessorTests {

        private IoTProcessor processor;

        @BeforeEach
        void setUp() {
            processor = new FilteringIoTProcessor(mockDataPort);
        }

        @Test
        @DisplayName("Should pass through online devices")
        void processDeviceData_withOnlineDevice_shouldPassThrough() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "online");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNotNull(result);
            assertEquals("DEV-001", result.getDeviceId());
        }

        @Test
        @DisplayName("Should filter out offline devices")
        void processDeviceData_withOfflineDevice_shouldReturnNull() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "offline");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNull(result);
        }

        @Test
        @DisplayName("Should filter offline devices from list")
        void processAllDevices_withMixedStatus_shouldFilterOffline() {
            List<IoTDeviceData> devices = List.of(
                createTestDevice("DEV-001", "Meter 1", "online"),
                createTestDevice("DEV-002", "Meter 2", "offline"),
                createTestDevice("DEV-003", "Meter 3", "online")
            );
            when(mockDataPort.fetchAllDevices()).thenReturn(devices);

            List<IoTDeviceData> results = processor.processAllDevices();

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(d -> "online".equalsIgnoreCase(d.getStatus())));
        }

        @Test
        @DisplayName("Should handle null data gracefully")
        void processDeviceData_withNullData_shouldReturnNull() {
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(null);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNull(result);
        }

        @Test
        @DisplayName("Should handle case-insensitive status matching")
        void processDeviceData_withUppercaseStatus_shouldFilterCorrectly() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "OFFLINE");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("EnrichedIoTProcessor Tests")
    class EnrichedIoTProcessorTests {

        private IoTProcessor processor;

        @BeforeEach
        void setUp() {
            processor = new EnrichedIoTProcessor(mockDataPort, "test-source");
        }

        @Test
        @DisplayName("Should enrich device name with source prefix")
        void processDeviceData_shouldEnrichDeviceName() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "online");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNotNull(result);
            assertTrue(result.getDeviceName().startsWith("[test-source]"));
            assertTrue(result.getDeviceName().contains("Smart Meter"));
        }

        @Test
        @DisplayName("Should handle null device name")
        void processDeviceData_withNullDeviceName_shouldCreateDefaultName() {
            IoTDeviceData mockData = createTestDevice("DEV-001", null, "online");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNotNull(result);
            assertTrue(result.getDeviceName().startsWith("[test-source]"));
            assertTrue(result.getDeviceName().contains("DEV-001"));
        }

        @Test
        @DisplayName("Should handle empty device name")
        void processDeviceData_withEmptyDeviceName_shouldCreateDefaultName() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "", "online");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNotNull(result);
            assertTrue(result.getDeviceName().startsWith("[test-source]"));
        }

        @Test
        @DisplayName("Should update timestamp to current time")
        void processDeviceData_shouldUpdateTimestamp() {
            long beforeProcessing = System.currentTimeMillis();
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "online");
            mockData.setTimestamp(beforeProcessing - 10000);
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertTrue(result.getTimestamp() >= beforeProcessing);
        }

        @Test
        @DisplayName("Should handle null data gracefully")
        void processDeviceData_withNullData_shouldReturnNull() {
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(null);

            IoTDeviceData result = processor.processDeviceData("DEV-001");

            assertNull(result);
        }

        @Test
        @DisplayName("Should enrich all devices in list")
        void processAllDevices_shouldEnrichAllDevices() {
            List<IoTDeviceData> devices = List.of(
                createTestDevice("DEV-001", "Meter 1", "online"),
                createTestDevice("DEV-002", "Meter 2", "online")
            );
            when(mockDataPort.fetchAllDevices()).thenReturn(devices);

            List<IoTDeviceData> results = processor.processAllDevices();

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(d -> d.getDeviceName().startsWith("[test-source]")));
        }
    }

    @Nested
    @DisplayName("Bridge Pattern Integration Tests")
    class BridgeIntegrationTests {

        @Test
        @DisplayName("Should use different processors with same data port")
        void differentProcessors_sameDataPort_shouldWorkIndependently() {
            IoTDeviceData mockData = createTestDevice("DEV-001", "Smart Meter", "offline");
            when(mockDataPort.fetchDeviceData("DEV-001")).thenReturn(mockData);

            IoTProcessor simpleProcessor = new SimpleIoTProcessor(mockDataPort);
            IoTProcessor filteringProcessor = new FilteringIoTProcessor(mockDataPort);
            IoTProcessor enrichedProcessor = new EnrichedIoTProcessor(mockDataPort, "integration");

            IoTDeviceData simpleResult = simpleProcessor.processDeviceData("DEV-001");
            IoTDeviceData filteringResult = filteringProcessor.processDeviceData("DEV-001");
            IoTDeviceData enrichedResult = enrichedProcessor.processDeviceData("DEV-001");

            assertNotNull(simpleResult);
            assertNull(filteringResult);
            assertNotNull(enrichedResult);
            assertTrue(enrichedResult.getDeviceName().startsWith("[integration]"));
        }

        @Test
        @DisplayName("Should delegate correctly to IoTDataPort")
        void processor_shouldDelegateToDataPort() {
            IoTProcessor processor = new SimpleIoTProcessor(mockDataPort);
            IoTDeviceData mockData = createTestDevice("DEL-001", "Delegated Device", "online");
            
            when(mockDataPort.fetchDeviceData("DEL-001")).thenReturn(mockData);
            when(mockDataPort.fetchAllDevices()).thenReturn(List.of(mockData));
            when(mockDataPort.fetchDevicesByLocation("TestLocation")).thenReturn(List.of(mockData));
            when(mockDataPort.fetchDevicesByType("meter")).thenReturn(List.of(mockData));

            processor.processDeviceData("DEL-001");
            processor.processAllDevices();
            processor.processDevicesByLocation("TestLocation");
            processor.processDevicesByType("meter");

            verify(mockDataPort).fetchDeviceData("DEL-001");
            verify(mockDataPort).fetchAllDevices();
            verify(mockDataPort).fetchDevicesByLocation("TestLocation");
            verify(mockDataPort).fetchDevicesByType("meter");
        }

        @Test
        @DisplayName("Should handle edge case: empty location query")
        void processor_withEmptyLocation_shouldReturnEmptyList() {
            IoTProcessor processor = new SimpleIoTProcessor(mockDataPort);
            when(mockDataPort.fetchDevicesByLocation("")).thenReturn(List.of());

            List<IoTDeviceData> results = processor.processDevicesByLocation("");

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should handle edge case: empty type query")
        void processor_withEmptyType_shouldReturnEmptyList() {
            IoTProcessor processor = new SimpleIoTProcessor(mockDataPort);
            when(mockDataPort.fetchDevicesByType("")).thenReturn(List.of());

            List<IoTDeviceData> results = processor.processDevicesByType("");

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should work with mock IoTDataPort implementation")
        void processor_withMockPort_shouldWork() {
            IoTDataPort customPort = mock(IoTDataPort.class);
            when(customPort.fetchDeviceData("MOCK-001")).thenReturn(
                createTestDevice("MOCK-001", "Mock Device", "online")
            );

            IoTProcessor processor = new SimpleIoTProcessor(customPort);
            IoTDeviceData result = processor.processDeviceData("MOCK-001");

            assertNotNull(result);
            assertEquals("MOCK-001", result.getDeviceId());
            verify(customPort).fetchDeviceData("MOCK-001");
        }
    }

    private IoTDeviceData createTestDevice(String id, String name, String status) {
        return new IoTDeviceData(id, name, DeviceType.SMART_METER, 100.0, "kWh", "Test Location", status, System.currentTimeMillis());
    }

    private IoTDeviceData createTestDeviceWithLocation(String id, String name, String location, String status) {
        return new IoTDeviceData(id, name, DeviceType.SMART_METER, 100.0, "kWh", location, status, System.currentTimeMillis());
    }

    private IoTDeviceData createTestDeviceWithType(String id, String name, DeviceType type, String status) {
        return new IoTDeviceData(id, name, type, 100.0, "kWh", "Test Location", status, System.currentTimeMillis());
    }
}
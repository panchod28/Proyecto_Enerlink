package com.enerlink.enerlink.iot.infraestructura.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.infraestructura.dto.SmartHomeProviderDTO;
import com.enerlink.enerlink.iot.infraestructura.mapper.IoTDataMapper;

@ExtendWith(MockitoExtension.class)
class SmartHomeAdapterTest {

    @Mock
    private SmartHomeProviderClient providerClient;

    private IoTDataMapper mapper;
    private SmartHomeAdapter adapter;

    @BeforeEach
    void setUp() {
        mapper = new IoTDataMapper();
        adapter = new SmartHomeAdapter(providerClient);
    }

    @Test
    void fetchDeviceData_withValidId_returnsDeviceData() {
        SmartHomeProviderDTO dto = createMockSmartHomeDTO("DEV-001", "Smart Thermostat", "thermostat", 22.5, "°C", "online");
        when(providerClient.fetchDeviceById("DEV-001")).thenReturn(Optional.of(dto));

        IoTDeviceData result = adapter.fetchDeviceData("DEV-001");

        assertNotNull(result);
        assertEquals("DEV-001", result.getDeviceId());
        assertEquals("Smart Thermostat", result.getDeviceName());
        assertEquals(DeviceType.THERMOSTAT, result.getDeviceType());
        assertEquals(22.5, result.getCurrentReading());
        assertEquals("°C", result.getUnit());
        assertEquals("online", result.getStatus());
    }

    @Test
    void fetchDeviceData_withNonExistentId_returnsNull() {
        when(providerClient.fetchDeviceById("NONEXISTENT")).thenReturn(Optional.empty());

        IoTDeviceData result = adapter.fetchDeviceData("NONEXISTENT");

        assertNull(result);
    }

    @Test
    void fetchAllDevices_returnsListOfDevices() {
        List<Object> mockDevices = List.of(
            createMockSmartHomeDTO("DEV-001", "Meter", "meter", 100.0, "kWh", "online"),
            createMockSmartHomeDTO("DEV-002", "Solar Panel", "solar", 250.0, "kWh", "online"),
            createMockSmartHomeDTO("DEV-003", "Battery", "battery", 75.0, "kWh", "online")
        );
        when(providerClient.fetchAllDevices()).thenReturn(mockDevices);

        List<IoTDeviceData> results = adapter.fetchAllDevices();

        assertEquals(3, results.size());
        assertEquals("DEV-001", results.get(0).getDeviceId());
        assertEquals("DEV-002", results.get(1).getDeviceId());
        assertEquals("DEV-003", results.get(2).getDeviceId());
    }

    @Test
    void fetchAllDevices_withEmptyProvider_returnsEmptyList() {
        when(providerClient.fetchAllDevices()).thenReturn(List.of());

        List<IoTDeviceData> results = adapter.fetchAllDevices();

        assertTrue(results.isEmpty());
    }

    @Test
    void fetchDevicesByLocation_withValidLocation_returnsDevice() {
        SmartHomeProviderDTO dto = createMockSmartHomeDTO("DEV-001", "Meter", "meter", 100.0, "kWh", "online");
        when(providerClient.fetchDeviceByLocation("Living Room")).thenReturn(Optional.of(dto));

        List<IoTDeviceData> results = adapter.fetchDevicesByLocation("Living Room");

        assertEquals(1, results.size());
        assertEquals("Living Room", results.get(0).getLocation());
    }

    @Test
    void fetchDevicesByLocation_withNonExistentLocation_returnsEmptyList() {
        when(providerClient.fetchDeviceByLocation("NonExistent")).thenReturn(Optional.empty());

        List<IoTDeviceData> results = adapter.fetchDevicesByLocation("NonExistent");

        assertTrue(results.isEmpty());
    }

    @Test
    void fetchDevicesByType_withValidType_returnsDevice() {
        SmartHomeProviderDTO dto = createMockSmartHomeDTO("DEV-001", "Smart Switch", "switch", 1.0, "on", "online");
        when(providerClient.fetchDeviceByType("switch")).thenReturn(Optional.of(dto));

        List<IoTDeviceData> results = adapter.fetchDevicesByType("switch");

        assertEquals(1, results.size());
        assertEquals(DeviceType.SMART_SWITCH, results.get(0).getDeviceType());
    }

    @Test
    void fetchDeviceData_withNullId_returnsNull() {
        when(providerClient.fetchDeviceById(null)).thenReturn(Optional.empty());

        IoTDeviceData result = adapter.fetchDeviceData(null);

        assertNull(result);
    }

    @Test
    void fetchDeviceData_mapsAllFieldsCorrectly() {
        SmartHomeProviderDTO dto = createMockSmartHomeDTO("SH-TEST", "EV Charger", "evcharger", 45.0, "kWh", "charging");
        when(providerClient.fetchDeviceById("SH-TEST")).thenReturn(Optional.of(dto));

        IoTDeviceData result = adapter.fetchDeviceData("SH-TEST");

        assertNotNull(result);
        assertEquals("SH-TEST", result.getDeviceId());
        assertEquals("EV Charger", result.getDeviceName());
        assertEquals(DeviceType.ELECTRIC_VEHICLE_CHARGER, result.getDeviceType());
        assertEquals(45.0, result.getCurrentReading());
        assertEquals("kWh", result.getUnit());
        assertEquals("charging", result.getStatus());
        assertEquals("Living Room", result.getLocation());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    void fetchDevicesByType_withNonExistentType_returnsEmptyList() {
        when(providerClient.fetchDeviceByType("nonexistent")).thenReturn(Optional.empty());

        List<IoTDeviceData> results = adapter.fetchDevicesByType("nonexistent");

        assertTrue(results.isEmpty());
    }

    private SmartHomeProviderDTO createMockSmartHomeDTO(String id, String name, String category,
                                                        double value, String unit, String state) {
        SmartHomeProviderDTO dto = new SmartHomeProviderDTO();
        dto.setDeviceId(id);
        dto.setDeviceName(name);
        dto.setDeviceCategory(category);

        SmartHomeProviderDTO.MetricValue metrics = new SmartHomeProviderDTO.MetricValue();
        metrics.setMeasurementType("energy_consumption");
        metrics.setValue(value);
        metrics.setUnitOfMeasure(unit);
        metrics.setQualityScore(0.95);
        dto.setMetrics(metrics);

        SmartHomeProviderDTO.LocationInfo location = new SmartHomeProviderDTO.LocationInfo();
        location.setSiteId("SITE-001");
        location.setZone("Living Room");
        location.setAddress("123 Main St");
        dto.setLocation(location);

        dto.setOperationalState(state);
        dto.setLastUpdated(System.currentTimeMillis());

        return dto;
    }
}
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
import com.enerlink.enerlink.iot.infraestructura.dto.EnergyCloudProviderDTO;
import com.enerlink.enerlink.iot.infraestructura.mapper.IoTDataMapper;

@ExtendWith(MockitoExtension.class)
class EnergyCloudAdapterTest {

    @Mock
    private EnergyCloudProviderClient providerClient;

    private IoTDataMapper mapper;
    private EnergyCloudAdapter adapter;

    @BeforeEach
    void setUp() {
        mapper = new IoTDataMapper();
        adapter = new EnergyCloudAdapter(providerClient);
    }

    @Test
    void fetchDeviceData_withValidId_returnsDeviceData() {
        EnergyCloudProviderDTO dto = createMockEnergyCloudDTO("EC-001", "Primary Meter", "meter", 2500.75, "kWh", true);
        when(providerClient.fetchDeviceById("EC-001")).thenReturn(Optional.of(dto));

        IoTDeviceData result = adapter.fetchDeviceData("EC-001");

        assertNotNull(result);
        assertEquals("EC-001", result.getDeviceId());
        assertEquals("Primary Meter", result.getDeviceName());
        assertEquals(DeviceType.SMART_METER, result.getDeviceType());
        assertEquals(2500.75, result.getCurrentReading());
        assertEquals("kWh", result.getUnit());
        assertEquals("ACTIVE", result.getStatus());
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
            createMockEnergyCloudDTO("EC-001", "Meter 1", "meter", 100.0, "kWh", true),
            createMockEnergyCloudDTO("EC-002", "Solar Array", "solar", 500.0, "kWh", true),
            createMockEnergyCloudDTO("EC-003", "Battery", "battery", 50.0, "kWh", true)
        );
        when(providerClient.fetchAllDevices()).thenReturn(mockDevices);

        List<IoTDeviceData> results = adapter.fetchAllDevices();

        assertEquals(3, results.size());
        assertEquals("EC-001", results.get(0).getDeviceId());
        assertEquals("EC-002", results.get(1).getDeviceId());
        assertEquals("EC-003", results.get(2).getDeviceId());
    }

    @Test
    void fetchAllDevices_withEmptyProvider_returnsEmptyList() {
        when(providerClient.fetchAllDevices()).thenReturn(List.of());

        List<IoTDeviceData> results = adapter.fetchAllDevices();

        assertTrue(results.isEmpty());
    }

    @Test
    void fetchDevicesByLocation_withValidLocation_returnsDevice() {
        EnergyCloudProviderDTO dto = createMockEnergyCloudDTO("EC-001", "Meter 1", "meter", 100.0, "kWh", true);
        when(providerClient.fetchDeviceByLocation("Zone-A")).thenReturn(Optional.of(dto));

        List<IoTDeviceData> results = adapter.fetchDevicesByLocation("Zone-A");

        assertEquals(1, results.size());
        assertEquals("EC-001", results.get(0).getDeviceId());
    }

    @Test
    void fetchDevicesByLocation_withNonExistentLocation_returnsEmptyList() {
        when(providerClient.fetchDeviceByLocation("NonExistent")).thenReturn(Optional.empty());

        List<IoTDeviceData> results = adapter.fetchDevicesByLocation("NonExistent");

        assertTrue(results.isEmpty());
    }

    @Test
    void fetchDevicesByType_withValidType_returnsDevice() {
        EnergyCloudProviderDTO dto = createMockEnergyCloudDTO("EC-001", "Meter 1", "meter", 100.0, "kWh", true);
        when(providerClient.fetchDeviceByType("meter")).thenReturn(Optional.of(dto));

        List<IoTDeviceData> results = adapter.fetchDevicesByType("meter");

        assertEquals(1, results.size());
        assertEquals(DeviceType.SMART_METER, results.get(0).getDeviceType());
    }

    @Test
    void fetchDeviceData_mapsAllFieldsCorrectly() {
        EnergyCloudProviderDTO dto = createMockEnergyCloudDTO("EC-TEST", "Solar Array PV", "solar", 1500.25, "kWh", true);
        when(providerClient.fetchDeviceById("EC-TEST")).thenReturn(Optional.of(dto));

        IoTDeviceData result = adapter.fetchDeviceData("EC-TEST");

        assertNotNull(result);
        assertEquals("EC-TEST", result.getDeviceId());
        assertEquals("Solar Array PV", result.getDeviceName());
        assertEquals(DeviceType.SOLAR_PANEL, result.getDeviceType());
        assertEquals(1500.25, result.getCurrentReading());
        assertEquals("kWh", result.getUnit());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    void fetchDeviceData_withNullId_returnsNull() {
        when(providerClient.fetchDeviceById(null)).thenReturn(Optional.empty());

        IoTDeviceData result = adapter.fetchDeviceData(null);

        assertNull(result);
    }

    private EnergyCloudProviderDTO createMockEnergyCloudDTO(String id, String name, String type, 
                                                            double value, String unit, boolean active) {
        EnergyCloudProviderDTO dto = new EnergyCloudProviderDTO();

        EnergyCloudProviderDTO.ResourceData resource = new EnergyCloudProviderDTO.ResourceData();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setManufacturer("EnergyCorp");
        resource.setModel("EC-2000");
        dto.setResource(resource);

        EnergyCloudProviderDTO.MeasurementData measurement = new EnergyCloudProviderDTO.MeasurementData();
        measurement.setCurrentValue(value);
        measurement.setUom(unit);
        measurement.setTimestamp(System.currentTimeMillis());
        dto.setMeasurement(measurement);

        EnergyCloudProviderDTO.StatusInfo status = new EnergyCloudProviderDTO.StatusInfo();
        status.setCode(active ? "ACTIVE" : "INACTIVE");
        status.setDescription(active ? "Device is operational" : "Device is offline");
        status.setActive(active);
        dto.setStatus(status);

        return dto;
    }
}
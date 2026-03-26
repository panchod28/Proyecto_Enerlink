package com.enerlink.enerlink.iot.infraestructura.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IoTDataPortCompositeTest {

    @Mock
    private EnergyCloudAdapter energyCloudAdapter;

    @Mock
    private SmartHomeAdapter smartHomeAdapter;

    private IoTDataPortComposite composite;

    @BeforeEach
    void setUp() {
        when(energyCloudAdapter.fetchAllDevices()).thenReturn(List.of());
        when(smartHomeAdapter.fetchAllDevices()).thenReturn(List.of());
        when(energyCloudAdapter.fetchDevicesByLocation(any())).thenReturn(List.of());
        when(smartHomeAdapter.fetchDevicesByLocation(any())).thenReturn(List.of());
        when(energyCloudAdapter.fetchDevicesByType(any())).thenReturn(List.of());
        when(smartHomeAdapter.fetchDevicesByType(any())).thenReturn(List.of());

        composite = new IoTDataPortComposite(List.of(energyCloudAdapter, smartHomeAdapter));
    }

    @Test
    void fetchDeviceData_byDeviceIdPrefix_ECDevice_returnsEnergyCloudAdapter() {
        when(energyCloudAdapter.fetchDeviceData("EC-001")).thenReturn(
            createDevice("EC-001", "Energy Device", DeviceType.SMART_METER));

        IoTDeviceData result = composite.fetchDeviceData("EC-001");

        assertNotNull(result);
        assertEquals("EC-001", result.getDeviceId());
        verify(energyCloudAdapter).fetchDeviceData("EC-001");
        verify(smartHomeAdapter, never()).fetchDeviceData(any());
    }

    @Test
    void fetchDeviceData_byDeviceIdPrefix_DEVDevice_returnsSmartHomeAdapter() {
        when(smartHomeAdapter.fetchDeviceData("DEV-001")).thenReturn(
            createDevice("DEV-001", "Smart Device", DeviceType.THERMOSTAT));

        IoTDeviceData result = composite.fetchDeviceData("DEV-001");

        assertNotNull(result);
        assertEquals("DEV-001", result.getDeviceId());
        verify(smartHomeAdapter).fetchDeviceData("DEV-001");
    }

    @Test
    void fetchDeviceData_withProviderParameter_usesSpecifiedProvider() {
        when(smartHomeAdapter.fetchDeviceData("DEV-001")).thenReturn(
            createDevice("DEV-001", "Smart Device", DeviceType.THERMOSTAT));

        composite.setSelectionStrategyByProvider("smart");

        IoTDeviceData result = composite.fetchDeviceData("DEV-001");

        assertNotNull(result);
        assertEquals("DEV-001", result.getDeviceId());
        verify(smartHomeAdapter).fetchDeviceData("DEV-001");
    }

    @Test
    void fetchDeviceData_withUnknownPrefix_returnsDefaultAdapter() {
        when(energyCloudAdapter.fetchDeviceData("UNKNOWN-001")).thenReturn(
            createDevice("UNKNOWN-001", "Unknown Device", DeviceType.SENSOR));

        IoTDeviceData result = composite.fetchDeviceData("UNKNOWN-001");

        assertNotNull(result);
        verify(energyCloudAdapter).fetchDeviceData("UNKNOWN-001");
    }

    @Test
    void fetchDeviceData_fallbackStrategy_triesAllAdapters() {
        when(energyCloudAdapter.fetchDeviceData("ANY-001")).thenReturn(null);
        when(smartHomeAdapter.fetchDeviceData("ANY-001")).thenReturn(
            createDevice("ANY-001", "Found Device", DeviceType.SOLAR_PANEL));

        composite.setSelectionStrategy(new FallbackSelectionStrategy());

        IoTDeviceData result = composite.fetchDeviceData("ANY-001");

        assertNotNull(result);
        assertEquals("ANY-001", result.getDeviceId());
        verify(energyCloudAdapter, atLeastOnce()).fetchDeviceData("ANY-001");
        verify(smartHomeAdapter, atLeastOnce()).fetchDeviceData("ANY-001");
    }

    @Test
    void fetchAllDevices_withDefaultStrategy_returnsFromFirstAdapter() {
        when(energyCloudAdapter.fetchAllDevices()).thenReturn(List.of(
            createDevice("EC-001", "Energy 1", DeviceType.SMART_METER)));

        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(energyCloudAdapter).fetchAllDevices();
    }

    @Test
    void getAvailableProviders_returnsAllAdapterNames() {
        List<String> providers = composite.getAvailableProviders();

        assertEquals(2, providers.size());
        assertTrue(providers.contains("EnergyCloudAdapter"));
        assertTrue(providers.contains("SmartHomeAdapter"));
    }

    @Test
    void fetchDeviceData_withNullDeviceId_returnsFromDefaultAdapter() {
        when(energyCloudAdapter.fetchDeviceData(null)).thenReturn(
            createDevice("NULL-001", "Default Device", DeviceType.SENSOR));

        IoTDeviceData result = composite.fetchDeviceData(null);

        assertNotNull(result);
        verify(energyCloudAdapter).fetchDeviceData(null);
    }

    @Test
    void fetchDeviceData_withSHPrefix_returnsSmartHomeAdapter() {
        when(smartHomeAdapter.fetchDeviceData("SH-001")).thenReturn(
            createDevice("SH-001", "SmartHome Device", DeviceType.SMART_SWITCH));

        IoTDeviceData result = composite.fetchDeviceData("SH-001");

        assertNotNull(result);
        assertEquals("SH-001", result.getDeviceId());
        verify(smartHomeAdapter).fetchDeviceData("SH-001");
    }

    private IoTDeviceData createDevice(String id, String name, DeviceType type) {
        return new IoTDeviceData(id, name, type, 100.0, "kWh", "test", "active", System.currentTimeMillis());
    }
}
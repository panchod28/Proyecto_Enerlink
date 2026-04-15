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
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IoTDataPortCompositeTest {

    @Mock
    private IoTDataPort adapter1;

    @Mock
    private IoTDataPort adapter2;

    @Mock
    private IoTDataPort adapter3;

    private IoTDataPortComposite composite;

    @BeforeEach
    void setUp() {
        composite = new IoTDataPortComposite();
    }

    private IoTDeviceData createDevice(String id, String name, DeviceType type) {
        return new IoTDeviceData(id, name, type, 100.0, "kWh", "test", "active", System.currentTimeMillis());
    }

    @Test
    void should_register_one_child_when_add_called() {
        composite.add(adapter1);

        List<IoTDataPort> children = composite.getChildren();

        assertEquals(1, children.size());
        assertTrue(children.contains(adapter1));
    }

    @Test
    void should_register_multiple_children_when_add_called_multiple_times() {
        composite.add(adapter1);
        composite.add(adapter2);
        composite.add(adapter3);

        List<IoTDataPort> children = composite.getChildren();

        assertEquals(3, children.size());
        assertTrue(children.contains(adapter1));
        assertTrue(children.contains(adapter2));
        assertTrue(children.contains(adapter3));
    }

    @Test
    void should_remove_correct_child_when_remove_called() {
        composite.add(adapter1);
        composite.add(adapter2);
        composite.add(adapter3);

        composite.remove(adapter2);

        List<IoTDataPort> children = composite.getChildren();
        assertEquals(2, children.size());
        assertTrue(children.contains(adapter1));
        assertFalse(children.contains(adapter2));
        assertTrue(children.contains(adapter3));
    }

    @Test
    void should_handle_noop_when_child_not_present_on_remove() {
        composite.add(adapter1);

        composite.remove(adapter2);

        List<IoTDataPort> children = composite.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.contains(adapter1));
    }

    @Test
    void should_return_all_registered_children_when_getChildren_called() {
        composite.add(adapter1);
        composite.add(adapter2);

        List<IoTDataPort> children = composite.getChildren();

        assertEquals(2, children.size());
    }

    @Test
    void should_return_empty_list_when_no_children_registered() {
        List<IoTDataPort> children = composite.getChildren();

        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void should_delegate_to_all_children_and_aggregate_results_when_fetchAllDevices() {
        when(adapter1.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-001", "Device 1", DeviceType.SMART_METER)));
        when(adapter2.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-002", "Device 2", DeviceType.THERMOSTAT)));
        when(adapter3.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-003", "Device 3", DeviceType.SENSOR)));

        composite.add(adapter1);
        composite.add(adapter2);
        composite.add(adapter3);

        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertEquals(3, results.size());
        verify(adapter1).fetchAllDevices();
        verify(adapter2).fetchAllDevices();
        verify(adapter3).fetchAllDevices();
    }

    @Test
    void should_return_only_child_data_when_single_child_present() {
        when(adapter1.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-001", "Device 1", DeviceType.SMART_METER)));

        composite.add(adapter1);

        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertEquals(1, results.size());
        assertEquals("DEV-001", results.get(0).getDeviceId());
    }

    @Test
    void should_return_empty_result_without_throwing_when_no_children() {
        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void should_call_each_child_exactly_once_per_fetch() {
        composite.add(adapter1);
        composite.add(adapter2);

        composite.fetchAllDevices();

        verify(adapter1, times(1)).fetchAllDevices();
        verify(adapter2, times(1)).fetchAllDevices();
    }

    @Test
    void should_call_child_once_when_added_twice_but_not_duplicated() {
        composite.add(adapter1);
        composite.add(adapter1);

        composite.fetchAllDevices();

        verify(adapter1, times(1)).fetchAllDevices();
        assertEquals(1, composite.getChildren().size());
    }

    @Test
    void should_interact_with_port_interface_when_processing() {
        composite.add(adapter1);
        when(adapter1.fetchAllDevices()).thenReturn(List.of());

        IoTDataPort port = composite;
        port.fetchAllDevices();

        verify(adapter1).fetchAllDevices();
    }

    @Test
    void should_return_data_from_remaining_adapters_when_one_removed() {
        when(adapter1.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-001", "Device 1", DeviceType.SMART_METER)));
        when(adapter2.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-002", "Device 2", DeviceType.THERMOSTAT)));

        composite.add(adapter1);
        composite.add(adapter2);
        composite.remove(adapter1);

        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertEquals(1, results.size());
        assertEquals("DEV-002", results.get(0).getDeviceId());
        verify(adapter1, never()).fetchAllDevices();
        verify(adapter2).fetchAllDevices();
    }

    @Test
    void should_handle_null_return_from_adapter_when_fetchAllDevices() {
        when(adapter1.fetchAllDevices()).thenReturn(null);
        when(adapter2.fetchAllDevices()).thenReturn(List.of(
            createDevice("DEV-002", "Device 2", DeviceType.THERMOSTAT)));

        composite.add(adapter1);
        composite.add(adapter2);

        List<IoTDeviceData> results = composite.fetchAllDevices();

        assertEquals(1, results.size());
        assertEquals("DEV-002", results.get(0).getDeviceId());
    }

    @Test
    void should_return_first_found_device_when_fetchDeviceData() {
        when(adapter1.fetchDeviceData("DEV-001")).thenReturn(null);
        when(adapter2.fetchDeviceData("DEV-001")).thenReturn(
            createDevice("DEV-001", "Device 1", DeviceType.SMART_METER));

        composite.add(adapter1);
        composite.add(adapter2);

        IoTDeviceData result = composite.fetchDeviceData("DEV-001");

        assertNotNull(result);
        assertEquals("DEV-001", result.getDeviceId());
        verify(adapter1).fetchDeviceData("DEV-001");
        verify(adapter2).fetchDeviceData("DEV-001");
    }

    @Test
    void should_return_null_when_device_not_found_in_any_adapter() {
        when(adapter1.fetchDeviceData("UNKNOWN")).thenReturn(null);
        when(adapter2.fetchDeviceData("UNKNOWN")).thenReturn(null);

        composite.add(adapter1);
        composite.add(adapter2);

        IoTDeviceData result = composite.fetchDeviceData("UNKNOWN");

        assertNull(result);
    }

    @Test
    void should_aggregate_devices_by_location_from_all_adapters() {
        when(adapter1.fetchDevicesByLocation("zone-a")).thenReturn(List.of(
            createDevice("DEV-001", "Device 1", DeviceType.SENSOR)));
        when(adapter2.fetchDevicesByLocation("zone-a")).thenReturn(List.of(
            createDevice("DEV-002", "Device 2", DeviceType.SENSOR)));

        composite.add(adapter1);
        composite.add(adapter2);

        List<IoTDeviceData> results = composite.fetchDevicesByLocation("zone-a");

        assertEquals(2, results.size());
        verify(adapter1).fetchDevicesByLocation("zone-a");
        verify(adapter2).fetchDevicesByLocation("zone-a");
    }

    @Test
    void should_aggregate_devices_by_type_from_all_adapters() {
        when(adapter1.fetchDevicesByType("thermostat")).thenReturn(List.of(
            createDevice("DEV-001", "Thermostat 1", DeviceType.THERMOSTAT)));
        when(adapter2.fetchDevicesByType("thermostat")).thenReturn(List.of(
            createDevice("DEV-002", "Thermostat 2", DeviceType.THERMOSTAT)));

        composite.add(adapter1);
        composite.add(adapter2);

        List<IoTDeviceData> results = composite.fetchDevicesByType("thermostat");

        assertEquals(2, results.size());
        verify(adapter1).fetchDevicesByType("thermostat");
        verify(adapter2).fetchDevicesByType("thermostat");
    }

    @Test
    void should_not_duplicate_adapter_when_added_twice() {
        composite.add(adapter1);
        composite.add(adapter1);

        List<IoTDataPort> children = composite.getChildren();

        assertEquals(1, children.size());
    }
}

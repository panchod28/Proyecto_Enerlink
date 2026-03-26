package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.enerlink.enerlink.iot.infraestructura.dto.SmartHomeProviderDTO;

public class SmartHomeProviderClient implements IoTProviderClient {

    private static final String PROVIDER_NAME = "SmartHome";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Optional<Object> fetchDeviceById(String deviceId) {
        SmartHomeProviderDTO dto = new SmartHomeProviderDTO();
        dto.setDeviceId(deviceId);
        dto.setDeviceName("Smart Device " + deviceId);
        dto.setDeviceCategory("smart_meter");
        
        SmartHomeProviderDTO.MetricValue metrics = new SmartHomeProviderDTO.MetricValue();
        metrics.setMeasurementType("energy_consumption");
        metrics.setValue(1234.56);
        metrics.setUnitOfMeasure("kWh");
        metrics.setQualityScore(0.98);
        dto.setMetrics(metrics);
        
        SmartHomeProviderDTO.LocationInfo location = new SmartHomeProviderDTO.LocationInfo();
        location.setSiteId("SITE-001");
        location.setZone("Living Room");
        location.setAddress("123 Main St");
        dto.setLocation(location);
        
        dto.setOperationalState("online");
        dto.setLastUpdated(System.currentTimeMillis());
        
        return Optional.of(dto);
    }

    @Override
    public List<Object> fetchAllDevices() {
        return List.of(
            createMockDevice("DEV-001", "Smart Meter 1", "meter"),
            createMockDevice("DEV-002", "Solar Panel Array", "solar"),
            createMockDevice("DEV-003", "Battery Storage", "battery")
        );
    }

    @Override
    public Optional<Object> fetchDeviceByLocation(String location) {
        List<Object> allDevices = fetchAllDevices();
        return allDevices.stream().findFirst();
    }

    @Override
    public Optional<Object> fetchDeviceByType(String deviceType) {
        List<Object> allDevices = fetchAllDevices();
        return allDevices.stream().findFirst();
    }

    private Object createMockDevice(String id, String name, String category) {
        SmartHomeProviderDTO dto = new SmartHomeProviderDTO();
        dto.setDeviceId(id);
        dto.setDeviceName(name);
        dto.setDeviceCategory(category);
        
        SmartHomeProviderDTO.MetricValue metrics = new SmartHomeProviderDTO.MetricValue();
        metrics.setMeasurementType("energy");
        metrics.setValue(100.0);
        metrics.setUnitOfMeasure("kWh");
        metrics.setQualityScore(1.0);
        dto.setMetrics(metrics);
        
        dto.setOperationalState("online");
        dto.setLastUpdated(System.currentTimeMillis());
        
        return dto;
    }
}
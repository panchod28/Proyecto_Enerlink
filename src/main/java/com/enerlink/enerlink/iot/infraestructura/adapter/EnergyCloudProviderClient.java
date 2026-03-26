package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.util.List;
import java.util.Optional;

import com.enerlink.enerlink.iot.infraestructura.dto.EnergyCloudProviderDTO;

public class EnergyCloudProviderClient implements IoTProviderClient {

    private static final String PROVIDER_NAME = "EnergyCloud";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Optional<Object> fetchDeviceById(String deviceId) {
        EnergyCloudProviderDTO dto = new EnergyCloudProviderDTO();

        EnergyCloudProviderDTO.ResourceData resource = new EnergyCloudProviderDTO.ResourceData();
        resource.setId(deviceId);
        resource.setName("Energy Device " + deviceId);
        resource.setType("meter");
        resource.setManufacturer("EnergyCorp");
        resource.setModel("EC-2000");
        dto.setResource(resource);

        EnergyCloudProviderDTO.MeasurementData measurement = new EnergyCloudProviderDTO.MeasurementData();
        measurement.setCurrentValue(2500.75);
        measurement.setUom("kWh");
        measurement.setTimestamp(System.currentTimeMillis());
        EnergyCloudProviderDTO.AccuracyInfo accuracy = new EnergyCloudProviderDTO.AccuracyInfo();
        accuracy.setPercentage(99.5);
        accuracy.setMethod("calibrated");
        measurement.setAccuracy(accuracy);
        dto.setMeasurement(measurement);

        EnergyCloudProviderDTO.StatusInfo status = new EnergyCloudProviderDTO.StatusInfo();
        status.setCode("ACTIVE");
        status.setDescription("Device is operational");
        status.setActive(true);
        dto.setStatus(status);

        return Optional.of(dto);
    }

    @Override
    public List<Object> fetchAllDevices() {
        return List.of(
            createMockResource("EC-001", "Primary Meter", "energy_meter"),
            createMockResource("EC-002", "Solar Array PV", "pv_array"),
            createMockResource("EC-003", "Storage System", "storage")
        );
    }

    @Override
    public Optional<Object> fetchDeviceByLocation(String location) {
        return fetchAllDevices().stream().findFirst();
    }

    @Override
    public Optional<Object> fetchDeviceByType(String deviceType) {
        return fetchAllDevices().stream().findFirst();
    }

    private Object createMockResource(String id, String name, String type) {
        EnergyCloudProviderDTO dto = new EnergyCloudProviderDTO();

        EnergyCloudProviderDTO.ResourceData resource = new EnergyCloudProviderDTO.ResourceData();
        resource.setId(id);
        resource.setName(name);
        resource.setType(type);
        resource.setManufacturer("EnergyCorp");
        resource.setModel("EC-2000");
        dto.setResource(resource);

        EnergyCloudProviderDTO.MeasurementData measurement = new EnergyCloudProviderDTO.MeasurementData();
        measurement.setCurrentValue(500.0);
        measurement.setUom("kWh");
        measurement.setTimestamp(System.currentTimeMillis());
        dto.setMeasurement(measurement);

        EnergyCloudProviderDTO.StatusInfo status = new EnergyCloudProviderDTO.StatusInfo();
        status.setCode("ACTIVE");
        status.setActive(true);
        dto.setStatus(status);

        return dto;
    }
}

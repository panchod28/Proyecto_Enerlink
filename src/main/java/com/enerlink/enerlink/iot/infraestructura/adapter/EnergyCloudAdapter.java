package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.infraestructura.dto.EnergyCloudProviderDTO;
import com.enerlink.enerlink.iot.infraestructura.mapper.IoTDataMapper;

public class EnergyCloudAdapter implements IoTDataPort {

    private final EnergyCloudProviderClient providerClient;
    private final IoTDataMapper mapper;

    public EnergyCloudAdapter(EnergyCloudProviderClient providerClient) {
        this.providerClient = providerClient;
        this.mapper = new IoTDataMapper();
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        Optional<Object> rawData = providerClient.fetchDeviceById(deviceId);
        
        if (rawData.isEmpty()) {
            return null;
        }

        EnergyCloudProviderDTO dto = convertToEnergyCloudDTO(rawData.get());
        return mapper.mapFromEnergyCloud(dto);
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        List<Object> rawDevices = providerClient.fetchAllDevices();
        List<IoTDeviceData> result = new ArrayList<>();

        for (Object raw : rawDevices) {
            EnergyCloudProviderDTO dto = convertToEnergyCloudDTO(raw);
            IoTDeviceData data = mapper.mapFromEnergyCloud(dto);
            if (data != null) {
                result.add(data);
            }
        }

        return result;
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        Optional<Object> rawData = providerClient.fetchDeviceByLocation(location);
        
        if (rawData.isEmpty()) {
            return List.of();
        }

        EnergyCloudProviderDTO dto = convertToEnergyCloudDTO(rawData.get());
        IoTDeviceData data = mapper.mapFromEnergyCloud(dto);
        
        return data != null ? List.of(data) : List.of();
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        Optional<Object> rawData = providerClient.fetchDeviceByType(deviceType);
        
        if (rawData.isEmpty()) {
            return List.of();
        }

        EnergyCloudProviderDTO dto = convertToEnergyCloudDTO(rawData.get());
        IoTDeviceData data = mapper.mapFromEnergyCloud(dto);
        
        return data != null ? List.of(data) : List.of();
    }

    private EnergyCloudProviderDTO convertToEnergyCloudDTO(Object raw) {
        if (raw instanceof EnergyCloudProviderDTO) {
            return (EnergyCloudProviderDTO) raw;
        }
        throw new IllegalArgumentException("Unsupported data format for EnergyCloud provider");
    }
}
package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.infraestructura.dto.SmartHomeProviderDTO;
import com.enerlink.enerlink.iot.infraestructura.mapper.IoTDataMapper;

public class SmartHomeAdapter implements IoTDataPort {

    private static final Logger logger = LoggerFactory.getLogger(SmartHomeAdapter.class);

    private final SmartHomeProviderClient providerClient;
    private final IoTDataMapper mapper;

    public SmartHomeAdapter(SmartHomeProviderClient providerClient) {
        this.providerClient = providerClient;
        this.mapper = new IoTDataMapper();
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        logger.info("Fetching device data from SmartHome provider for device: {}", deviceId);

        Optional<Object> rawData = providerClient.fetchDeviceById(deviceId);
        
        if (rawData.isEmpty()) {
            logger.warn("Device not found: {}", deviceId);
            return null;
        }

        SmartHomeProviderDTO dto = convertToSmartHomeDTO(rawData.get());
        return mapper.mapFromSmartHome(dto);
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        logger.info("Fetching all devices from SmartHome provider");

        List<Object> rawDevices = providerClient.fetchAllDevices();
        List<IoTDeviceData> result = new ArrayList<>();

        for (Object raw : rawDevices) {
            SmartHomeProviderDTO dto = convertToSmartHomeDTO(raw);
            IoTDeviceData data = mapper.mapFromSmartHome(dto);
            if (data != null) {
                result.add(data);
            }
        }

        return result;
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        logger.info("Fetching devices by location from SmartHome provider: {}", location);

        Optional<Object> rawData = providerClient.fetchDeviceByLocation(location);
        
        if (rawData.isEmpty()) {
            return List.of();
        }

        SmartHomeProviderDTO dto = convertToSmartHomeDTO(rawData.get());
        IoTDeviceData data = mapper.mapFromSmartHome(dto);
        
        return data != null ? List.of(data) : List.of();
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        logger.info("Fetching devices by type from SmartHome provider: {}", deviceType);

        Optional<Object> rawData = providerClient.fetchDeviceByType(deviceType);
        
        if (rawData.isEmpty()) {
            return List.of();
        }

        SmartHomeProviderDTO dto = convertToSmartHomeDTO(rawData.get());
        IoTDeviceData data = mapper.mapFromSmartHome(dto);
        
        return data != null ? List.of(data) : List.of();
    }

    private SmartHomeProviderDTO convertToSmartHomeDTO(Object raw) {
        if (raw instanceof SmartHomeProviderDTO) {
            return (SmartHomeProviderDTO) raw;
        }
        throw new IllegalArgumentException("Unsupported data format for SmartHome provider");
    }
}
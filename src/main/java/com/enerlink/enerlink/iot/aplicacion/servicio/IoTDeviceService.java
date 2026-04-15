package com.enerlink.enerlink.iot.aplicacion.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

@Service
public class IoTDeviceService {

    private static final Logger logger = LoggerFactory.getLogger(IoTDeviceService.class);

    private final IoTDataPort ioTDataPort;

    public IoTDeviceService(IoTDataPort ioTDataPort) {
        this.ioTDataPort = ioTDataPort;
    }

    public IoTDeviceData getDeviceData(String deviceId) {
        logger.info("Service: Fetching device data for device: {}", deviceId);
        return ioTDataPort.fetchDeviceData(deviceId);
    }

    public IoTDeviceData getDeviceData(String deviceId, String provider) {
        logger.info("Service: Fetching device data for device: {} from provider: {}", deviceId, provider);
        return ioTDataPort.fetchDeviceData(deviceId);
    }

    public List<IoTDeviceData> getAllDevices() {
        logger.info("Service: Fetching all devices");
        return ioTDataPort.fetchAllDevices();
    }

    public List<IoTDeviceData> getDevicesByLocation(String location) {
        logger.info("Service: Fetching devices by location: {}", location);
        return ioTDataPort.fetchDevicesByLocation(location);
    }

    public List<IoTDeviceData> getDevicesByType(String deviceType) {
        logger.info("Service: Fetching devices by type: {}", deviceType);
        return ioTDataPort.fetchDevicesByType(deviceType);
    }
}
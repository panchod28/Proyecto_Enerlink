package com.enerlink.enerlink.iot.aplicacion.servicio;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDeviceRepositoryPort;

@Service
public class IoTDeviceService {

    private static final Logger logger = LoggerFactory.getLogger(IoTDeviceService.class);

    private final IoTDataPort ioTDataPort;
    private final IoTDeviceRepositoryPort ioTDeviceRepositoryPort;

    public IoTDeviceService(IoTDataPort ioTDataPort, IoTDeviceRepositoryPort ioTDeviceRepositoryPort) {
        this.ioTDataPort = ioTDataPort;
        this.ioTDeviceRepositoryPort = ioTDeviceRepositoryPort;
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

    public IoTDeviceData createDevice(IoTDeviceData device) {
        logger.info("Service: Creating device with deviceId: {}", device.getDeviceId());
        return ioTDeviceRepositoryPort.save(device);
    }

    public IoTDeviceData updateDevice(Long id, IoTDeviceData device) {
        logger.info("Service: Updating device with id: {}", id);
        ioTDeviceRepositoryPort.deleteById(id);
        IoTDeviceData saved = ioTDeviceRepositoryPort.save(device);
        return saved;
    }

    public void deleteDevice(Long id) {
        logger.info("Service: Deleting device with id: {}", id);
        ioTDeviceRepositoryPort.deleteById(id);
    }

    public List<IoTDeviceData> getDevicesByUserId(Long userId) {
        logger.info("Service: Fetching devices by userId: {}", userId);
        return ioTDeviceRepositoryPort.findByUserId(userId);
    }
}

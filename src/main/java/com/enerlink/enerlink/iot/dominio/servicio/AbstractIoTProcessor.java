package com.enerlink.enerlink.iot.dominio.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.dominio.modelo.IoTProcessor;

public abstract class AbstractIoTProcessor implements IoTProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final IoTDataPort dataPort;

    protected AbstractIoTProcessor(IoTDataPort dataPort) {
        this.dataPort = dataPort;
    }

    @Override
    public IoTDeviceData processDeviceData(String deviceId) {
        logger.info("Processing device data for: {}", deviceId);
        IoTDeviceData data = dataPort.fetchDeviceData(deviceId);
        return transformDeviceData(data);
    }

    @Override
    public List<IoTDeviceData> processAllDevices() {
        logger.info("Processing all devices");
        List<IoTDeviceData> devices = dataPort.fetchAllDevices();
        return transformDeviceList(devices);
    }

    @Override
    public List<IoTDeviceData> processDevicesByLocation(String location) {
        logger.info("Processing devices by location: {}", location);
        List<IoTDeviceData> devices = dataPort.fetchDevicesByLocation(location);
        return transformDeviceList(devices);
    }

    @Override
    public List<IoTDeviceData> processDevicesByType(String deviceType) {
        logger.info("Processing devices by type: {}", deviceType);
        List<IoTDeviceData> devices = dataPort.fetchDevicesByType(deviceType);
        return transformDeviceList(devices);
    }

    protected abstract IoTDeviceData transformDeviceData(IoTDeviceData data);

    protected List<IoTDeviceData> transformDeviceList(List<IoTDeviceData> devices) {
        return devices.stream()
                .map(this::transformDeviceData)
                .filter(d -> d != null)
                .toList();
    }
}
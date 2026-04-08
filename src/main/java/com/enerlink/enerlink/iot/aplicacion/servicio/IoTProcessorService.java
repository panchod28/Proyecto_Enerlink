package com.enerlink.enerlink.iot.aplicacion.servicio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.modelo.IoTProcessor;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.infraestructura.adapter.IoTDataPortComposite;

@Service
public class IoTProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(IoTProcessorService.class);

    private final IoTProcessor defaultProcessor;
    private final IoTDataPort ioTDataPort;

    public IoTProcessorService(IoTProcessor defaultProcessor, IoTDataPort ioTDataPort) {
        this.defaultProcessor = defaultProcessor;
        this.ioTDataPort = ioTDataPort;
    }

    public IoTProcessor getDefaultProcessor() {
        return defaultProcessor;
    }

    public IoTProcessor getProcessorForProvider(String provider) {
        logger.debug("Getting processor for provider: {}", provider);
        if (provider == null) {
            return defaultProcessor;
        }
        return defaultProcessor;
    }

    public IoTDeviceData processDeviceData(String deviceId) {
        logger.info("Processing device data for: {}", deviceId);
        return defaultProcessor.processDeviceData(deviceId);
    }

    public IoTDeviceData processDeviceData(String deviceId, String provider) {
        logger.info("Processing device data for device: {} with provider strategy: {}", deviceId, provider);
        IoTProcessor processor = getProcessorForProvider(provider);
        
        if (provider != null && ioTDataPort instanceof IoTDataPortComposite composite) {
            composite.setSelectionStrategyByProvider(provider);
        }
        
        return processor.processDeviceData(deviceId);
    }

    public List<IoTDeviceData> processAllDevices() {
        logger.info("Processing all devices");
        return defaultProcessor.processAllDevices();
    }

    public List<IoTDeviceData> processDevicesByLocation(String location) {
        logger.info("Processing devices by location: {}", location);
        return defaultProcessor.processDevicesByLocation(location);
    }

    public List<IoTDeviceData> processDevicesByType(String deviceType) {
        logger.info("Processing devices by type: {}", deviceType);
        return defaultProcessor.processDevicesByType(deviceType);
    }
}
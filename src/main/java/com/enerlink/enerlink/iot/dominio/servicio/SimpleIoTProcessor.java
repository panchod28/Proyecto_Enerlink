package com.enerlink.enerlink.iot.dominio.servicio;

import java.time.Instant;
import java.util.List;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

public class SimpleIoTProcessor extends AbstractIoTProcessor {

    public SimpleIoTProcessor(IoTDataPort dataPort) {
        super(dataPort);
    }

    @Override
    protected IoTDeviceData transformDeviceData(IoTDeviceData data) {
        if (data == null) {
            return null;
        }
        logger.debug("Simple processor: returning raw data for device: {}", data.getDeviceId());
        return data;
    }
}
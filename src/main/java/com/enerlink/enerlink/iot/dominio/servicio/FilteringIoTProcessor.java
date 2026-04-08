package com.enerlink.enerlink.iot.dominio.servicio;

import java.util.List;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

public class FilteringIoTProcessor extends AbstractIoTProcessor {

    private static final String OFFLINE_STATUS = "offline";

    public FilteringIoTProcessor(IoTDataPort dataPort) {
        super(dataPort);
    }

    @Override
    protected IoTDeviceData transformDeviceData(IoTDeviceData data) {
        if (data == null) {
            return null;
        }
        
        if (OFFLINE_STATUS.equalsIgnoreCase(data.getStatus())) {
            logger.info("Filtering out device {} due to offline status", data.getDeviceId());
            return null;
        }

        logger.debug("Filtering processor: device {} passed status filter", data.getDeviceId());
        return data;
    }
}
package com.enerlink.enerlink.iot.dominio.servicio;

import java.time.Instant;
import java.util.List;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;

public class EnrichedIoTProcessor extends AbstractIoTProcessor {

    private final String enrichmentSource;

    public EnrichedIoTProcessor(IoTDataPort dataPort, String enrichmentSource) {
        super(dataPort);
        this.enrichmentSource = enrichmentSource != null ? enrichmentSource : "default";
    }

    @Override
    protected IoTDeviceData transformDeviceData(IoTDeviceData data) {
        if (data == null) {
            return null;
        }

        data.setDeviceName(enrichDeviceName(data.getDeviceName(), data.getDeviceId()));
        data.setTimestamp(Instant.now().toEpochMilli());

        logger.debug("Enriched processor: device {} enriched from source: {}", 
            data.getDeviceId(), enrichmentSource);
        
        return data;
    }

    private String enrichDeviceName(String originalName, String deviceId) {
        String baseName = (originalName == null || originalName.isBlank()) 
            ? "Device-" + deviceId 
            : originalName;
        return "[" + enrichmentSource + "] " + baseName;
    }
}
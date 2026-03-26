package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceIdPrefixSelectionStrategy implements AdapterSelectionStrategy {

    private static final Map<String, String> PREFIX_TO_ADAPTER = new HashMap<>();
    
    static {
        PREFIX_TO_ADAPTER.put("ec", "energycloud");
        PREFIX_TO_ADAPTER.put("dev", "smarthome");
        PREFIX_TO_ADAPTER.put("sh", "smarthome");
    }

    private final Map<String, IoTDataPort> adapterMap;

    public DeviceIdPrefixSelectionStrategy(List<IoTDataPort> adapters) {
        this.adapterMap = new HashMap<>();
        for (IoTDataPort adapter : adapters) {
            String className = adapter.getClass().getSimpleName().toLowerCase();
            String key = className.replace("adapter", "");
            adapterMap.put(key, adapter);
        }
    }

    @Override
    public IoTDataPort select(List<IoTDataPort> adapters, String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return adapters.isEmpty() ? null : adapters.get(0);
        }

        String prefix = extractPrefix(deviceId).toLowerCase();
        String adapterKey = PREFIX_TO_ADAPTER.getOrDefault(prefix, prefix);
        
        IoTDataPort selected = adapterMap.get(adapterKey);
        return selected != null ? selected : (adapters.isEmpty() ? null : adapters.get(0));
    }

    private String extractPrefix(String deviceId) {
        int dashIndex = deviceId.indexOf('-');
        if (dashIndex > 0) {
            return deviceId.substring(0, dashIndex);
        }
        return deviceId.length() >= 2 ? deviceId.substring(0, 2) : deviceId;
    }
}
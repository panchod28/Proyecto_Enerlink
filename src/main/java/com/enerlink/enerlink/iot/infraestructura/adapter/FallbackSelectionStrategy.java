package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import java.util.List;

public class FallbackSelectionStrategy implements AdapterSelectionStrategy {

    @Override
    public IoTDataPort select(List<IoTDataPort> adapters, String deviceId) {
        if (adapters == null || adapters.isEmpty()) {
            return null;
        }
        
        for (IoTDataPort adapter : adapters) {
            IoTDeviceData result = adapter.fetchDeviceData(deviceId);
            if (result != null) {
                return adapter;
            }
        }
        
        return adapters.get(0);
    }

    @Override
    public IoTDataPort selectByLocation(List<IoTDataPort> adapters, String location) {
        if (adapters == null || adapters.isEmpty()) {
            return null;
        }
        
        for (IoTDataPort adapter : adapters) {
            List<IoTDeviceData> results = adapter.fetchDevicesByLocation(location);
            if (results != null && !results.isEmpty()) {
                return adapter;
            }
        }
        
        return adapters.get(0);
    }

    @Override
    public IoTDataPort selectByType(List<IoTDataPort> adapters, String deviceType) {
        if (adapters == null || adapters.isEmpty()) {
            return null;
        }
        
        for (IoTDataPort adapter : adapters) {
            List<IoTDeviceData> results = adapter.fetchDevicesByType(deviceType);
            if (results != null && !results.isEmpty()) {
                return adapter;
            }
        }
        
        return adapters.get(0);
    }
}
package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import java.util.List;

public interface AdapterSelectionStrategy {

    IoTDataPort select(List<IoTDataPort> adapters, String deviceId);
    
    default IoTDataPort selectByLocation(List<IoTDataPort> adapters, String location) {
        return select(adapters, location);
    }
    
    default IoTDataPort selectByType(List<IoTDataPort> adapters, String deviceType) {
        return select(adapters, deviceType);
    }
}
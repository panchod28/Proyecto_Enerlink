package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import java.util.List;

public class ProviderParameterSelectionStrategy implements AdapterSelectionStrategy {

    private final String providerName;
    private final List<IoTDataPort> adapters;

    public ProviderParameterSelectionStrategy(List<IoTDataPort> adapters, String providerName) {
        this.adapters = adapters;
        this.providerName = providerName != null ? providerName.toLowerCase() : null;
    }

    @Override
    public IoTDataPort select(List<IoTDataPort> adapters, String deviceId) {
        if (providerName == null || providerName.isEmpty()) {
            return adapters.isEmpty() ? null : adapters.get(0);
        }
        
        return adapters.stream()
            .filter(a -> a.getClass().getSimpleName().toLowerCase().contains(providerName))
            .findFirst()
            .orElse(adapters.isEmpty() ? null : adapters.get(0));
    }
}
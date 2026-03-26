package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import java.util.List;

public class IoTDataPortComposite implements IoTDataPort {

    private final List<IoTDataPort> adapters;
    private AdapterSelectionStrategy selectionStrategy;

    public IoTDataPortComposite(List<IoTDataPort> adapterList) {
        this.adapters = adapterList;
        this.selectionStrategy = new DeviceIdPrefixSelectionStrategy(adapterList);
    }

    public void setSelectionStrategy(AdapterSelectionStrategy strategy) {
        this.selectionStrategy = strategy;
    }

    public void setSelectionStrategyByProvider(String providerName) {
        this.selectionStrategy = new ProviderParameterSelectionStrategy(adapters, providerName);
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        IoTDataPort adapter = selectionStrategy.select(adapters, deviceId);
        return adapter != null ? adapter.fetchDeviceData(deviceId) : null;
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        IoTDataPort adapter = selectionStrategy.select(adapters, null);
        return adapter != null ? adapter.fetchAllDevices() : List.of();
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        IoTDataPort adapter = selectionStrategy.selectByLocation(adapters, location);
        return adapter != null ? adapter.fetchDevicesByLocation(location) : List.of();
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        IoTDataPort adapter = selectionStrategy.selectByType(adapters, deviceType);
        return adapter != null ? adapter.fetchDevicesByType(deviceType) : List.of();
    }

    public List<String> getAvailableProviders() {
        return adapters.stream()
            .map(a -> a.getClass().getSimpleName())
            .toList();
    }

    public AdapterSelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }
}
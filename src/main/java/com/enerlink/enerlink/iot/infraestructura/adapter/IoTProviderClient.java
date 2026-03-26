package com.enerlink.enerlink.iot.infraestructura.adapter;

import java.util.List;
import java.util.Optional;

public interface IoTProviderClient {

    String getProviderName();

    Optional<Object> fetchDeviceById(String deviceId);

    List<Object> fetchAllDevices();

    Optional<Object> fetchDeviceByLocation(String location);

    Optional<Object> fetchDeviceByType(String deviceType);
}
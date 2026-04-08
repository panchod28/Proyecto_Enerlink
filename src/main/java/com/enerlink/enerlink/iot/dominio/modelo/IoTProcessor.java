package com.enerlink.enerlink.iot.dominio.modelo;

import java.util.List;

public interface IoTProcessor {

    IoTDeviceData processDeviceData(String deviceId);

    List<IoTDeviceData> processAllDevices();

    List<IoTDeviceData> processDevicesByLocation(String location);

    List<IoTDeviceData> processDevicesByType(String deviceType);
}
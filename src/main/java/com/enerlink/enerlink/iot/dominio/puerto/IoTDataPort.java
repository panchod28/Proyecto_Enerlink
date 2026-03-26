package com.enerlink.enerlink.iot.dominio.puerto;

import java.util.List;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;

public interface IoTDataPort {

    IoTDeviceData fetchDeviceData(String deviceId);

    List<IoTDeviceData> fetchAllDevices();

    List<IoTDeviceData> fetchDevicesByLocation(String location);

    List<IoTDeviceData> fetchDevicesByType(String deviceType);
}
package com.enerlink.enerlink.iot.dominio.puerto;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;

import java.util.List;
import java.util.Optional;

public interface IoTDeviceRepositoryPort {

    IoTDeviceData save(IoTDeviceData device);

    List<IoTDeviceData> findAll();

    List<IoTDeviceData> findByUserId(Long userId);

    Optional<IoTDeviceData> findById(Long id);

    Optional<IoTDeviceData> findByDeviceId(String deviceId);

    void deleteById(Long id);
}

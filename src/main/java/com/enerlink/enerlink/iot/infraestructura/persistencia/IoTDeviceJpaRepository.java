package com.enerlink.enerlink.iot.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IoTDeviceJpaRepository extends JpaRepository<IoTDeviceEntity, Long> {

    List<IoTDeviceEntity> findByUserId(Long userId);

    Optional<IoTDeviceEntity> findByDeviceId(String deviceId);
}

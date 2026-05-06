package com.enerlink.enerlink.iot.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface IoTDeviceJpaRepository extends JpaRepository<IoTDeviceEntity, Long> {

    List<IoTDeviceEntity> findByUserId(Long userId);

    Optional<IoTDeviceEntity> findByDeviceId(String deviceId);

    @Query(value = """
        SELECT
            user_id,
            SUM(current_reading) AS totalGeneration,
            COUNT(*)             AS deviceCount
        FROM iot_device
        WHERE device_type IN ('SOLAR_PANEL', 'BATTERY_STORAGE')
          AND status = 'online'
        GROUP BY user_id
        """, nativeQuery = true)
    List<Object[]> findGenerationByUser();

    @Query(value = """
        SELECT
            user_id,
            SUM(current_reading) AS totalConsumptionIoT,
            COUNT(*)             AS deviceCount
        FROM iot_device
        WHERE device_type IN ('SMART_METER', 'SENSOR', 'THERMOSTAT',
                              'SMART_SWITCH', 'ELECTRIC_VEHICLE_CHARGER')
          AND status = 'online'
        GROUP BY user_id
        """, nativeQuery = true)
    List<Object[]> findConsumptionByUser();
}

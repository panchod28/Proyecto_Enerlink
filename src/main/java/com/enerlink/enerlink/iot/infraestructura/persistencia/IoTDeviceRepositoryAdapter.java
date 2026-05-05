package com.enerlink.enerlink.iot.infraestructura.persistencia;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDeviceRepositoryPort;
import com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IoTDeviceRepositoryAdapter implements IoTDataPort, IoTDeviceRepositoryPort {

    private final IoTDeviceJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    public IoTDeviceRepositoryAdapter(IoTDeviceJpaRepository jpaRepository,
            UserJpaRepository userJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    private IoTDeviceData toDomain(IoTDeviceEntity entity) {
        IoTDeviceData data = new IoTDeviceData(
                entity.getDeviceId(),
                entity.getDeviceName(),
                entity.getDeviceType(),
                entity.getCurrentReading(),
                entity.getUnit(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getTimestamp());
        data.setId(entity.getId());
        data.setUserId(entity.getUserId());
        return data;
    }

    private IoTDeviceEntity toEntity(IoTDeviceData data) {
        IoTDeviceEntity entity = new IoTDeviceEntity();
        entity.setId(null);
        entity.setDeviceId(data.getDeviceId());
        entity.setDeviceName(data.getDeviceName());
        entity.setDeviceType(data.getDeviceType());
        entity.setCurrentReading(data.getCurrentReading());
        entity.setUnit(data.getUnit());
        entity.setLocation(data.getLocation());
        entity.setStatus(data.getStatus());
        entity.setTimestamp(data.getTimestamp());
        entity.setUserId(data.getUserId());
        return entity;
    }

    private IoTDeviceEntity toEntityForUpdate(Long id, IoTDeviceData data) {
        IoTDeviceEntity entity = new IoTDeviceEntity();
        entity.setId(id);
        entity.setDeviceId(data.getDeviceId());
        entity.setDeviceName(data.getDeviceName());
        entity.setDeviceType(data.getDeviceType());
        entity.setCurrentReading(data.getCurrentReading());
        entity.setUnit(data.getUnit());
        entity.setLocation(data.getLocation());
        entity.setStatus(data.getStatus());
        entity.setTimestamp(data.getTimestamp());
        entity.setUserId(data.getUserId());
        return entity;
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        return jpaRepository.findByDeviceId(deviceId)
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        return jpaRepository.findAll()
                .stream()
                .filter(entity -> location != null && location.equals(entity.getLocation()))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        return jpaRepository.findAll()
                .stream()
                .filter(entity -> deviceType != null &&
                        entity.getDeviceType() != null &&
                        deviceType.equalsIgnoreCase(entity.getDeviceType().name()))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public IoTDeviceData save(IoTDeviceData device) {
        if (device.getUserId() != null) {
            userJpaRepository.findById(device.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        IoTDeviceEntity entity;
        if (device.getId() != null) {
            // UPDATE — preservar el ID existente
            entity = toEntityForUpdate(device.getId(), device);
        } else {
            // INSERT — nuevo dispositivo
            entity = toEntity(device);
        }

        IoTDeviceEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<IoTDeviceData> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<IoTDeviceData> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<IoTDeviceData> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<IoTDeviceData> findByDeviceId(String deviceId) {
        return jpaRepository.findByDeviceId(deviceId)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
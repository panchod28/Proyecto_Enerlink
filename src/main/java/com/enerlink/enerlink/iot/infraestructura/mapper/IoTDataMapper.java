package com.enerlink.enerlink.iot.infraestructura.mapper;

import com.enerlink.enerlink.iot.dominio.modelo.DeviceType;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.infraestructura.dto.EnergyCloudProviderDTO;
import com.enerlink.enerlink.iot.infraestructura.dto.SmartHomeProviderDTO;

public class IoTDataMapper {

    public IoTDeviceData mapFromSmartHome(SmartHomeProviderDTO dto) {
        if (dto == null) {
            return null;
        }

        IoTDeviceData data = new IoTDeviceData();
        data.setDeviceId(dto.getDeviceId());
        data.setDeviceName(dto.getDeviceName());
        data.setDeviceType(mapDeviceCategory(dto.getDeviceCategory()));
        
        if (dto.getMetrics() != null) {
            data.setCurrentReading(dto.getMetrics().getValue());
            data.setUnit(dto.getMetrics().getUnitOfMeasure());
        }

        if (dto.getLocation() != null) {
            String location = dto.getLocation().getZone();
            if (location == null || location.isEmpty()) {
                location = dto.getLocation().getAddress();
            }
            data.setLocation(location);
        }

        data.setStatus(dto.getOperationalState());
        data.setTimestamp(dto.getLastUpdated());

        return data;
    }

    public IoTDeviceData mapFromEnergyCloud(EnergyCloudProviderDTO dto) {
        if (dto == null) {
            return null;
        }

        IoTDeviceData data = new IoTDeviceData();

        if (dto.getResource() != null) {
            data.setDeviceId(dto.getResource().getId());
            data.setDeviceName(dto.getResource().getName());
            data.setDeviceType(mapResourceType(dto.getResource().getType()));
        }

        if (dto.getMeasurement() != null) {
            data.setCurrentReading(dto.getMeasurement().getCurrentValue());
            data.setUnit(dto.getMeasurement().getUom());
            data.setTimestamp(dto.getMeasurement().getTimestamp());
        }

        if (dto.getStatus() != null) {
            data.setStatus(dto.getStatus().isActive() ? "ACTIVE" : "INACTIVE");
        }

        return data;
    }

    private DeviceType mapDeviceCategory(String category) {
        if (category == null) {
            return DeviceType.SENSOR;
        }

        return switch (category.toLowerCase()) {
            case "meter", "smart_meter" -> DeviceType.SMART_METER;
            case "solar", "solar_panel", "pv" -> DeviceType.SOLAR_PANEL;
            case "battery", "battery_storage" -> DeviceType.BATTERY_STORAGE;
            case "evcharger", "ev_charger" -> DeviceType.ELECTRIC_VEHICLE_CHARGER;
            case "thermostat" -> DeviceType.THERMOSTAT;
            case "switch", "smart_switch" -> DeviceType.SMART_SWITCH;
            default -> DeviceType.SENSOR;
        };
    }

    private DeviceType mapResourceType(String type) {
        if (type == null) {
            return DeviceType.SENSOR;
        }

        return switch (type.toLowerCase()) {
            case "meter", "energy_meter" -> DeviceType.SMART_METER;
            case "solar", "pv_array" -> DeviceType.SOLAR_PANEL;
            case "battery", "storage" -> DeviceType.BATTERY_STORAGE;
            case "evse", "charger" -> DeviceType.ELECTRIC_VEHICLE_CHARGER;
            case "hvac", "temperature" -> DeviceType.THERMOSTAT;
            case "relay", "switch" -> DeviceType.SMART_SWITCH;
            default -> DeviceType.SENSOR;
        };
    }
}
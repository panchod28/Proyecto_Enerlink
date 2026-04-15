package com.enerlink.enerlink.iot.infraestructura.adapter;

import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación Composite del patrón IoTDataPort.
 *
 * Este componente actúa como un Composite en el patrón Composite de Gang of Four,
 * permitiendo tratar múltiples adaptadores de forma uniforme como si fueran uno solo.
 *
 * El patrón Composite permite:
 * - Componer objetos en estructuras de árbol para representar jerarquías parte-todo
 * - Tratamiento uniforme de objetos individuales (Leaf) y composiciones (Composite)
 * - Agregación transparente de datos desde múltiples fuentes (adaptadores de marca)
 *
 * En el contexto de Enerlink, este Composite agregará datos de todos los adaptadores
 * de marcas (SmartHome, EnergyCloud, etc.) proporcionando una única interfaz para
 * obtener datos IoT sin que el cliente conozca la cantidad de fuentes.
 */
@Component
public class IoTDataPortComposite implements IoTDataPort {

    private static final Logger logger = LoggerFactory.getLogger(IoTDataPortComposite.class);

    private final List<IoTDataPort> children;

    public IoTDataPortComposite() {
        this.children = new ArrayList<>();
    }

    public void add(IoTDataPort child) {
        if (child != null && !children.contains(child)) {
            children.add(child);
            logger.debug("Adapter added to composite: {}", child.getClass().getSimpleName());
        }
    }

    public void remove(IoTDataPort child) {
        if (child != null) {
            children.remove(child);
            logger.debug("Adapter removed from composite: {}", child.getClass().getSimpleName());
        }
    }

    public List<IoTDataPort> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public IoTDeviceData fetchDeviceData(String deviceId) {
        if (children.isEmpty()) {
            logger.warn("No adapters available in composite for device: {}", deviceId);
            return null;
        }

        for (IoTDataPort adapter : children) {
            IoTDeviceData data = adapter.fetchDeviceData(deviceId);
            if (data != null) {
                logger.debug("Device {} found via adapter: {}", deviceId, adapter.getClass().getSimpleName());
                return data;
            }
        }

        logger.debug("Device {} not found in any adapter", deviceId);
        return null;
    }

    @Override
    public List<IoTDeviceData> fetchAllDevices() {
        if (children.isEmpty()) {
            return List.of();
        }

        List<IoTDeviceData> aggregated = new ArrayList<>();
        for (IoTDataPort adapter : children) {
            List<IoTDeviceData> devices = adapter.fetchAllDevices();
            if (devices != null) {
                aggregated.addAll(devices);
            }
        }

        logger.debug("Aggregated {} devices from {} adapters", aggregated.size(), children.size());
        return aggregated;
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByLocation(String location) {
        if (children.isEmpty()) {
            return List.of();
        }

        List<IoTDeviceData> aggregated = new ArrayList<>();
        for (IoTDataPort adapter : children) {
            List<IoTDeviceData> devices = adapter.fetchDevicesByLocation(location);
            if (devices != null) {
                aggregated.addAll(devices);
            }
        }

        logger.debug("Aggregated {} devices by location {} from {} adapters",
                aggregated.size(), location, children.size());
        return aggregated;
    }

    @Override
    public List<IoTDeviceData> fetchDevicesByType(String deviceType) {
        if (children.isEmpty()) {
            return List.of();
        }

        List<IoTDeviceData> aggregated = new ArrayList<>();
        for (IoTDataPort adapter : children) {
            List<IoTDeviceData> devices = adapter.fetchDevicesByType(deviceType);
            if (devices != null) {
                aggregated.addAll(devices);
            }
        }

        logger.debug("Aggregated {} devices by type {} from {} adapters",
                aggregated.size(), deviceType, children.size());
        return aggregated;
    }
}

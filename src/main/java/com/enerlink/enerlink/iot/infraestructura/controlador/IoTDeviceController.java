package com.enerlink.enerlink.iot.infraestructura.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.iot.aplicacion.servicio.IoTDeviceService;
import com.enerlink.enerlink.iot.dominio.modelo.IoTDeviceData;

@RestController
@RequestMapping("/api/iot/devices")
public class IoTDeviceController {

    private final IoTDeviceService ioTDeviceService;

    public IoTDeviceController(IoTDeviceService ioTDeviceService) {
        this.ioTDeviceService = ioTDeviceService;
    }

    @GetMapping("/{deviceId}")
    public IoTDeviceData getDevice(@PathVariable String deviceId,
                                   @RequestParam(required = false) String provider) {
        if (provider != null && !provider.isEmpty()) {
            return ioTDeviceService.getDeviceData(deviceId, provider);
        }
        return ioTDeviceService.getDeviceData(deviceId);
    }

    @GetMapping
    public List<IoTDeviceData> getAllDevices() {
        return ioTDeviceService.getAllDevices();
    }

    @GetMapping("/location/{location}")
    public List<IoTDeviceData> getDevicesByLocation(@PathVariable String location) {
        return ioTDeviceService.getDevicesByLocation(location);
    }

    @GetMapping("/type")
    public List<IoTDeviceData> getDevicesByType(@RequestParam String type) {
        return ioTDeviceService.getDevicesByType(type);
    }
}
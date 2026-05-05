package com.enerlink.enerlink.iot.infraestructura.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public IoTDeviceData createDevice(@RequestBody IoTDeviceData device) {
        return ioTDeviceService.createDevice(device);
    }

    @PutMapping("/{id}")
    public IoTDeviceData updateDevice(@PathVariable Long id, @RequestBody IoTDeviceData device) {
        return ioTDeviceService.updateDevice(id, device);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        ioTDeviceService.deleteDevice(id);
    }

    @GetMapping("/user/{userId}")
    public List<IoTDeviceData> getDevicesByUserId(@PathVariable Long userId) {
        return ioTDeviceService.getDevicesByUserId(userId);
    }
}

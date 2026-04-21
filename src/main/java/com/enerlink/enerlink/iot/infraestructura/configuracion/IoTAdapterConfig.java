package com.enerlink.enerlink.iot.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.infraestructura.adapter.CachingIoTDataProxy;
import com.enerlink.enerlink.iot.infraestructura.adapter.EnergyCloudAdapter;
import com.enerlink.enerlink.iot.infraestructura.adapter.EnergyCloudProviderClient;
import com.enerlink.enerlink.iot.infraestructura.adapter.IoTDataPortComposite;
import com.enerlink.enerlink.iot.infraestructura.adapter.SmartHomeAdapter;
import com.enerlink.enerlink.iot.infraestructura.adapter.SmartHomeProviderClient;

import java.time.Duration;

@Configuration
public class IoTAdapterConfig {

    @Bean
    public SmartHomeProviderClient smartHomeProviderClient() {
        return new SmartHomeProviderClient();
    }

    @Bean
    public EnergyCloudProviderClient energyCloudProviderClient() {
        return new EnergyCloudProviderClient();
    }

    @Bean
    public SmartHomeAdapter smartHomeAdapter(SmartHomeProviderClient smartHomeProviderClient) {
        return new SmartHomeAdapter(smartHomeProviderClient);
    }

    @Bean
    public EnergyCloudAdapter energyCloudAdapter(EnergyCloudProviderClient energyCloudProviderClient) {
        return new EnergyCloudAdapter(energyCloudProviderClient);
    }

    @Bean
    public IoTDataPortComposite ioTDataPortComposite(SmartHomeAdapter smartHomeAdapter, EnergyCloudAdapter energyCloudAdapter) {
        IoTDataPortComposite composite = new IoTDataPortComposite();
        composite.add(smartHomeAdapter);
        composite.add(energyCloudAdapter);
        return composite;
    }

    @Bean
    @Primary
    public IoTDataPort ioTDataPort(IoTDataPortComposite ioTDataPortComposite) {
        return new CachingIoTDataProxy(ioTDataPortComposite, Duration.ofMinutes(5));
    }
}
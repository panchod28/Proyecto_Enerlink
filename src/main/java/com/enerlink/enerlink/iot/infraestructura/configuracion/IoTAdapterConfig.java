package com.enerlink.enerlink.iot.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.infraestructura.adapter.EnergyCloudAdapter;
import com.enerlink.enerlink.iot.infraestructura.adapter.EnergyCloudProviderClient;
import com.enerlink.enerlink.iot.infraestructura.adapter.IoTDataPortComposite;
import com.enerlink.enerlink.iot.infraestructura.adapter.SmartHomeAdapter;
import com.enerlink.enerlink.iot.infraestructura.adapter.SmartHomeProviderClient;

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
    @Primary
    public IoTDataPort ioTDataPort(SmartHomeAdapter smartHomeAdapter, EnergyCloudAdapter energyCloudAdapter) {
        IoTDataPortComposite composite = new IoTDataPortComposite();
        composite.add(smartHomeAdapter);
        composite.add(energyCloudAdapter);
        return composite;
    }
}
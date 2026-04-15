package com.enerlink.enerlink.iot.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.enerlink.enerlink.iot.dominio.modelo.IoTProcessor;
import com.enerlink.enerlink.iot.dominio.puerto.IoTDataPort;
import com.enerlink.enerlink.iot.dominio.servicio.EnrichedIoTProcessor;
import com.enerlink.enerlink.iot.dominio.servicio.FilteringIoTProcessor;
import com.enerlink.enerlink.iot.dominio.servicio.SimpleIoTProcessor;

@Configuration
public class IoTProcessorConfig {

    @Bean
    public SimpleIoTProcessor simpleIoTProcessor(IoTDataPort ioTDataPort) {
        return new SimpleIoTProcessor(ioTDataPort);
    }

    @Bean
    public FilteringIoTProcessor filteringIoTProcessor(IoTDataPort ioTDataPort) {
        return new FilteringIoTProcessor(ioTDataPort);
    }

    @Bean
    public EnrichedIoTProcessor enrichedIoTProcessor(IoTDataPort ioTDataPort) {
        return new EnrichedIoTProcessor(ioTDataPort, "enerlink-platform");
    }

    @Bean
    @Primary
    public IoTProcessor defaultIoTProcessor(SimpleIoTProcessor simpleIoTProcessor) {
        return simpleIoTProcessor;
    }
}
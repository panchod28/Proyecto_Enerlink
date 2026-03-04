package com.enerlink.enerlink.energia.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.enerlink.enerlink.energia.dominio.factory.AuctionSaleFactory;
import com.enerlink.enerlink.energia.dominio.factory.DirectSaleFactory;
import com.enerlink.enerlink.energia.dominio.factory.EnergySaleFactory;

@Configuration
public class EnergySaleFactoryConfig {

    @Bean
    public EnergySaleFactory directSaleFactory() {
        return new DirectSaleFactory();
    }

    @Bean
    public EnergySaleFactory auctionSaleFactory() {
        return new AuctionSaleFactory();
    }
}

package com.enerlink.enerlink.energia.dominio.factory;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.proceso.DirectSaleProcess;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class DirectSaleFactory implements EnergySaleFactory {

    @Override
    public SaleType supports() {
        return SaleType.DIRECT;
    }

    @Override
    public EnergyOffer createEnergyOffer(Long id, User producer, double kwh, double price) {
        return new EnergyOffer(id, producer, kwh, price, SaleType.DIRECT);
    }

    @Override
    public SaleProcess createSaleProcess() {
        return new DirectSaleProcess();
    }
}

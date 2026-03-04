package com.enerlink.enerlink.energia.dominio.factory;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.proceso.AuctionSaleProcess;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class AuctionSaleFactory implements EnergySaleFactory {

    @Override
    public SaleType supports() {
        return SaleType.AUCTION;
    }

    @Override
    public EnergyOffer createEnergyOffer(Long id, User producer, double kwh, double price) {
        return new EnergyOffer(id, producer, kwh, price, SaleType.AUCTION);
    }

    @Override
    public SaleProcess createSaleProcess() {
        return new AuctionSaleProcess();
    }
}

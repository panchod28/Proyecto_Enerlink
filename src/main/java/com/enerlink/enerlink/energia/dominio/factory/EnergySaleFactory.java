package com.enerlink.enerlink.energia.dominio.factory;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public interface EnergySaleFactory {

    SaleType supports();

    EnergyOffer createEnergyOffer(Long id, User producer, double kwh, double price);

    SaleProcess createSaleProcess();

}

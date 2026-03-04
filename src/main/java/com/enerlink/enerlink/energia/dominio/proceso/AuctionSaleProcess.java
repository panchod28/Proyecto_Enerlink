package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;

public class AuctionSaleProcess implements SaleProcess {

    @Override
    public void execute(EnergyOffer offer) {
        // lógica de subasta
        // aquí se crearía la Auction
        System.out.println("Ejecutando subasta...");
    }
}

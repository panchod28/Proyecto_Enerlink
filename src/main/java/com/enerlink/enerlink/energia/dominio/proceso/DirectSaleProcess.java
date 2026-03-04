package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;

public class DirectSaleProcess implements SaleProcess {

    @Override
    public void execute(EnergyOffer offer) {
        // lógica de venta directa
        // aquí se crearía la Transaction
        System.out.println("Ejecutando venta directa...");
    }
}

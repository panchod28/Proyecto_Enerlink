package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;

public interface SaleProcess {

    void execute(EnergyOffer offer);

}

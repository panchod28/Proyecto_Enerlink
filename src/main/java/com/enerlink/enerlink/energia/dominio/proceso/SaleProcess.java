package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public interface SaleProcess {

    Transaction execute(EnergyOffer offer, User buyer, double kwh);

    default Transaction execute(EnergyOffer offer, User buyer, double kwh, double customPrice) {
        throw new UnsupportedOperationException("Custom price not supported for this sale type");
    }
}

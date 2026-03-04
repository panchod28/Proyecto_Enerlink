package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class Transaction {

    private EnergyOffer offer;
    private User buyer;
    private double finalPrice;

    public Transaction(EnergyOffer offer, User buyer, double finalPrice) {
        this.offer = offer;
        this.buyer = buyer;
        this.finalPrice = finalPrice;
    }
}

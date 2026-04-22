package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.time.LocalDateTime;

public class AuctionSaleProcess implements SaleProcess {

    @Override
    public Transaction execute(EnergyOffer offer, User buyer, double kwh) {
        return execute(offer, buyer, kwh, offer.getPrice());
    }

    @Override
    public Transaction execute(EnergyOffer offer, User buyer, double kwh, double customPrice) {
        Transaction transaction = Transaction.builder()
            .offer(offer)
            .buyer(buyer)
            .seller(offer.getProducer())
            .kwh(kwh)
            .price(customPrice)
            .timestamp(LocalDateTime.now())
            .build();

        System.out.println("Subasta ganada por: " + buyer.getNombre() + " - Precio final: " + transaction.getTotalAmount());
        return transaction;
    }
}
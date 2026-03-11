package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.time.LocalDateTime;

public class AuctionSaleProcess implements SaleProcess {

    @Override
    public Transaction execute(EnergyOffer offer, User buyer, double kwh) {
        double finalPrice = calculateWinningBid(offer);
        
        Transaction transaction = Transaction.builder()
            .offer(offer)
            .buyer(buyer)
            .seller(offer.getProducer())
            .kwh(kwh)
            .price(finalPrice)
            .timestamp(LocalDateTime.now())
            .build();
        
        System.out.println("Subasta ganada por: " + buyer.getNombre() + " - Precio final: " + transaction.getTotalAmount());
        return transaction;
    }

    private double calculateWinningBid(EnergyOffer offer) {
        return offer.getPrice() * 0.9;
    }
}

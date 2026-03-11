package com.enerlink.enerlink.energia.dominio.proceso;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.time.LocalDateTime;

public class DirectSaleProcess implements SaleProcess {

    @Override
    public Transaction execute(EnergyOffer offer, User buyer, double kwh) {
        double price = offer.getPrice();
        
        Transaction transaction = Transaction.builder()
            .offer(offer)
            .buyer(buyer)
            .seller(offer.getProducer())
            .kwh(kwh)
            .price(price)
            .timestamp(LocalDateTime.now())
            .build();
        
        System.out.println("Venta directa completada: " + transaction.getTotalAmount());
        return transaction;
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
        
        System.out.println("Venta directa completada con precio personalizado: " + transaction.getTotalAmount());
        return transaction;
    }
}

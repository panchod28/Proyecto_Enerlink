package com.enerlink.enerlink.energia.dominio.componente;

import java.time.LocalDateTime;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class ConcreteTransactionComponent implements TransactionComponent {

    private final Transaction transaction;

    public ConcreteTransactionComponent(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public Long getId() {
        return transaction.getId();
    }

    @Override
    public EnergyOffer getOffer() {
        return transaction.getOffer();
    }

    @Override
    public User getBuyer() {
        return transaction.getBuyer();
    }

    @Override
    public User getSeller() {
        return transaction.getSeller();
    }

    @Override
    public double getKwh() {
        return transaction.getKwh();
    }

    @Override
    public double getPrice() {
        return transaction.getPrice();
    }

    @Override
    public double getTotalAmount() {
        return transaction.getTotalAmount();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return transaction.getTimestamp();
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

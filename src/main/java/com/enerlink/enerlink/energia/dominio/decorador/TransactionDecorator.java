package com.enerlink.enerlink.energia.dominio.decorador;

import java.time.LocalDateTime;

import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;
import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public abstract class TransactionDecorator implements TransactionComponent {

    protected final TransactionComponent wrappedComponent;

    protected TransactionDecorator(TransactionComponent wrappedComponent) {
        if (wrappedComponent == null) {
            throw new IllegalArgumentException("Wrapped component cannot be null");
        }
        this.wrappedComponent = wrappedComponent;
    }

    @Override
    public Long getId() {
        return wrappedComponent.getId();
    }

    @Override
    public EnergyOffer getOffer() {
        return wrappedComponent.getOffer();
    }

    @Override
    public User getBuyer() {
        return wrappedComponent.getBuyer();
    }

    @Override
    public User getSeller() {
        return wrappedComponent.getSeller();
    }

    @Override
    public double getKwh() {
        return wrappedComponent.getKwh();
    }

    @Override
    public double getPrice() {
        return wrappedComponent.getPrice();
    }

    @Override
    public double getTotalAmount() {
        return wrappedComponent.getTotalAmount();
    }

    @Override
    public LocalDateTime getTimestamp() {
        return wrappedComponent.getTimestamp();
    }

    public TransactionComponent getWrapped() {
        return wrappedComponent;
    }
}

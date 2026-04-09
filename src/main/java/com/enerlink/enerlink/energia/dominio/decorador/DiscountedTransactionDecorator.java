package com.enerlink.enerlink.energia.dominio.decorador;

import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;

public class DiscountedTransactionDecorator extends TransactionDecorator {

    private final double discountPercentage;

    public DiscountedTransactionDecorator(TransactionComponent wrappedComponent, double discountPercentage) {
        super(wrappedComponent);
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double getPrice() {
        double basePrice = super.getPrice();
        double discountAmount = basePrice * (discountPercentage / 100.0);
        return basePrice - discountAmount;
    }

    @Override
    public double getTotalAmount() {
        return getKwh() * getPrice();
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getDiscountAmount() {
        return super.getTotalAmount() - getTotalAmount();
    }
}

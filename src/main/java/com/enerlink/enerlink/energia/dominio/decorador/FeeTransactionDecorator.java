package com.enerlink.enerlink.energia.dominio.decorador;

import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;

public class FeeTransactionDecorator extends TransactionDecorator {

    private final double fixedFee;
    private final double percentageFee;

    public FeeTransactionDecorator(TransactionComponent wrappedComponent, double fixedFee) {
        this(wrappedComponent, fixedFee, 0.0);
    }

    public FeeTransactionDecorator(TransactionComponent wrappedComponent, double fixedFee, double percentageFee) {
        super(wrappedComponent);
        if (fixedFee < 0) {
            throw new IllegalArgumentException("Fixed fee cannot be negative");
        }
        if (percentageFee < 0 || percentageFee > 100) {
            throw new IllegalArgumentException("Percentage fee must be between 0 and 100");
        }
        this.fixedFee = fixedFee;
        this.percentageFee = percentageFee;
    }

    @Override
    public double getTotalAmount() {
        double baseTotal = getKwh() * getPrice();
        double percentageAmount = baseTotal * (percentageFee / 100.0);
        return baseTotal + fixedFee + percentageAmount;
    }

    public double getFixedFee() {
        return fixedFee;
    }

    public double getPercentageFee() {
        return percentageFee;
    }

    public double getFeeAmount() {
        double baseTotal = getKwh() * getPrice();
        double percentageAmount = baseTotal * (percentageFee / 100.0);
        return fixedFee + percentageAmount;
    }
}

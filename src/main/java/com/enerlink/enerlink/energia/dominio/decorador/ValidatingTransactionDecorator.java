package com.enerlink.enerlink.energia.dominio.decorador;

import java.util.ArrayList;
import java.util.List;

import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class ValidatingTransactionDecorator extends TransactionDecorator {

    private final List<String> validationErrors;

    public ValidatingTransactionDecorator(TransactionComponent wrappedComponent) {
        super(wrappedComponent);
        this.validationErrors = new ArrayList<>();
    }

    @Override
    public User getBuyer() {
        User buyer = super.getBuyer();
        validateBuyer(buyer);
        return buyer;
    }

    @Override
    public User getSeller() {
        User seller = super.getSeller();
        validateSeller(seller);
        return seller;
    }

    private void validateBuyer(User buyer) {
        if (buyer == null) {
            validationErrors.add("Buyer cannot be null");
        }
        if (buyer != null && buyer.getNombre() == null) {
            validationErrors.add("Buyer must have a valid name");
        }
    }

    private void validateSeller(User seller) {
        if (seller == null) {
            validationErrors.add("Seller cannot be null");
        }
        if (seller != null && seller.getNombre() == null) {
            validationErrors.add("Seller must have a valid name");
        }
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }
}

package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Transaction {

    private final Long id;
    private final EnergyOffer offer;
    private final User buyer;
    private final User seller;
    private final double kwh;
    private final double price;
    private final LocalDateTime timestamp;

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.offer = builder.offer;
        this.buyer = builder.buyer;
        this.seller = builder.seller;
        this.kwh = builder.kwh;
        this.price = builder.price;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public EnergyOffer getOffer() {
        return offer;
    }

    public User getBuyer() {
        return buyer;
    }

    public User getSeller() {
        return seller;
    }

    public double getKwh() {
        return kwh;
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getTotalAmount() {
        return kwh * price;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", offer=" + offer +
                ", buyer=" + buyer +
                ", seller=" + seller +
                ", kwh=" + kwh +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class Builder {
        private Long id;
        private EnergyOffer offer;
        private User buyer;
        private User seller;
        private double kwh;
        private double price;
        private LocalDateTime timestamp;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder offer(EnergyOffer offer) {
            this.offer = offer;
            return this;
        }

        public Builder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public Builder seller(User seller) {
            this.seller = seller;
            return this;
        }

        public Builder kwh(double kwh) {
            this.kwh = kwh;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Transaction build() {
            validateMandatoryFields();
            return new Transaction(this);
        }

        private void validateMandatoryFields() {
            Objects.requireNonNull(offer, "Offer is mandatory");
            Objects.requireNonNull(buyer, "Buyer is mandatory");
            Objects.requireNonNull(seller, "Seller is mandatory");
            
            if (kwh <= 0) {
                throw new IllegalArgumentException("KWh must be greater than zero");
            }
            
            if (price <= 0) {
                throw new IllegalArgumentException("Price must be greater than zero");
            }
        }
    }
}

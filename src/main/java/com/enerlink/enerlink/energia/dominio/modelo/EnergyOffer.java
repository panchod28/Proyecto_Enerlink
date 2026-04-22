package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class EnergyOffer implements Prototype<EnergyOffer> {

    private Long id;
    private User producer;
    private double kwh;
    private double price;
    private SaleType saleType;
    private boolean available = true;

    public EnergyOffer() {
    }

    public EnergyOffer(Long id, User producer, double kwh, double price, SaleType saleType) {
        this.id = id;
        this.producer = producer;
        this.kwh = kwh;
        this.price = price;
        this.saleType = saleType;
        this.available = true;
    }

    public EnergyOffer(EnergyOffer original) {
        if (original == null) {
            throw new IllegalArgumentException("Original EnergyOffer cannot be null");
        }
        this.id = original.id;
        this.producer = original.producer;
        this.kwh = original.kwh;
        this.price = original.price;
        this.saleType = original.saleType;
        this.available = original.available;
    }

    @Override
    public EnergyOffer clone() {
        return new EnergyOffer(this);
    }

    @Override
    public EnergyOffer shallowClone() {
        EnergyOffer copy = new EnergyOffer();
        copy.id = this.id;
        copy.producer = this.producer;
        copy.kwh = this.kwh;
        copy.price = this.price;
        copy.saleType = this.saleType;
        copy.available = this.available;
        return copy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getProducer() { return producer; }
    public void setProducer(User producer) { this.producer = producer; }

    public double getKwh() { return kwh; }
    public void setKwh(double kwh) { this.kwh = kwh; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public SaleType getSaleType() { return saleType; }
    public void setSaleType(SaleType saleType) { this.saleType = saleType; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public EnergyOffer cloneWithNewId(Long newId) {
        EnergyOffer clone = this.clone();
        clone.setId(newId);
        return clone;
    }

    public EnergyOffer cloneWithNewPrice(double newPrice) {
        EnergyOffer clone = this.clone();
        clone.setPrice(newPrice);
        return clone;
    }

    public EnergyOffer cloneWithNewKwh(double newKwh) {
        EnergyOffer clone = this.clone();
        clone.setKwh(newKwh);
        return clone;
    }

    public EnergyOffer cloneWithNewSaleType(SaleType newSaleType) {
        EnergyOffer clone = this.clone();
        clone.setSaleType(newSaleType);
        return clone;
    }

    @Override
    public String toString() {
        return "EnergyOffer [id=" + id + ", producer=" + producer + ", kwh=" + kwh + ", price=" + price + ", saleType="+ saleType + ", available=" + available + "]";
    }
}
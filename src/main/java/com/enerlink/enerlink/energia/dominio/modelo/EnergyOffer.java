package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

public class EnergyOffer {

    private Long id;
    private User producer;
    private double kwh;
    private double price;
    private SaleType saleType;

    public EnergyOffer(Long id, User producer, double kwh, double price, SaleType saleType) {
        this.id = id;
        this.producer = producer;
        this.kwh = kwh;
        this.price = price;
        this.saleType = saleType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getProducer() {
        return producer;
    }

    public void setProducer(User producer) {
        this.producer = producer;
    }

    public double getKwh() {
        return kwh;
    }

    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

    @Override
    public String toString() {
        return "EnergyOffer [id=" + id + ", producer=" + producer + ", kwh=" + kwh + ", price=" + price + ", saleType="
                + saleType + "]";
    }

    

}

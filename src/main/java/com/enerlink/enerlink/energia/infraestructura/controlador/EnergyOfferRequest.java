package com.enerlink.enerlink.energia.infraestructura.controlador;

import com.enerlink.enerlink.energia.dominio.modelo.SaleType;

public class EnergyOfferRequest {

    private SaleType saleType;
    private Long producerId;
    private double kwh;
    private double price;
    public SaleType getSaleType() {
        return saleType;
    }
    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }
    public Long getProducerId() {
        return producerId;
    }
    public void setProducerId(Long producerId) {
        this.producerId = producerId;
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

    
    
}

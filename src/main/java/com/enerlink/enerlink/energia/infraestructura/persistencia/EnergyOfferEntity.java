package com.enerlink.enerlink.energia.infraestructura.persistencia;

import com.enerlink.enerlink.energia.dominio.modelo.SaleType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "energy_offer")
public class EnergyOfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long producerId;
    private double kwh;
    private double price;

    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    // getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SaleType getSaleType() {
        return saleType;
    }

    public void setSaleType(SaleType saleType) {
        this.saleType = saleType;
    }

    @Override
    public String toString() {
        return "EnergyOfferEntity{" +
                "id=" + id +
                ", producerId=" + producerId +
                ", kwh=" + kwh +
                ", price=" + price +
                ", saleType=" + saleType +
                '}';
    }
}

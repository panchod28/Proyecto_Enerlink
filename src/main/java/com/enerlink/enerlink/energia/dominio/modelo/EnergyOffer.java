package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * Domain model representing an energy offer in the Enerlink marketplace.
 * 
 * This class implements the Prototype design pattern to support efficient
 * creation of copies of energy offers. The pattern is particularly useful when:
 * - Creating new offers based on existing templates
 * - Modifying offers without affecting the original
 * - Preserving immutability of shared data while allowing variations
 * 
 * The class provides both deep and shallow cloning capabilities:
 * - Deep clone: Creates independent copies including the producer reference
 * - Shallow clone: Shares the producer reference with the original
 */
public class EnergyOffer implements Prototype<EnergyOffer> {

    private Long id;
    private User producer;
    private double kwh;
    private double price;
    private SaleType saleType;

    /**
     * Default constructor for JPA and other frameworks.
     */
    public EnergyOffer() {
    }

    /**
     * Creates a new EnergyOffer with the specified parameters.
     *
     * @param id        the unique identifier of the offer
     * @param producer  the user offering the energy
     * @param kwh       the amount of energy in kilowatt-hours
     * @param price     the price per kWh
     * @param saleType  the type of sale (DIRECT or AUCTION)
     */
    public EnergyOffer(Long id, User producer, double kwh, double price, SaleType saleType) {
        this.id = id;
        this.producer = producer;
        this.kwh = kwh;
        this.price = price;
        this.saleType = saleType;
    }

    /**
     * Copy constructor for creating deep clones.
     * Creates an independent copy of the original offer.
     * 
     * Note: The producer is shared (not deep cloned) because User is abstract
     * and the concrete implementation might have complex internal state.
     * If deep cloning of User is needed, consider implementing Prototype on User.
     *
     * @param original the EnergyOffer to copy
     */
    public EnergyOffer(EnergyOffer original) {
        if (original == null) {
            throw new IllegalArgumentException("Original EnergyOffer cannot be null");
        }
        this.id = original.id;
        this.producer = original.producer;
        this.kwh = original.kwh;
        this.price = original.price;
        this.saleType = original.saleType;
    }

    @Override
    public EnergyOffer clone() {
        return new EnergyOffer(this);
    }

    @Override
    public EnergyOffer shallowClone() {
        if (this == null) {
            throw new IllegalStateException("Cannot clone null instance");
        }
        EnergyOffer copy = new EnergyOffer();
        copy.id = this.id;
        copy.producer = this.producer;
        copy.kwh = this.kwh;
        copy.price = this.price;
        copy.saleType = this.saleType;
        return copy;
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

    /**
     * Creates a clone with a new ID assigned.
     * Useful for creating new offers based on templates.
     *
     * @param newId the ID for the cloned offer
     * @return a new EnergyOffer with the specified ID
     */
    public EnergyOffer cloneWithNewId(Long newId) {
        EnergyOffer clone = this.clone();
        clone.setId(newId);
        return clone;
    }

    /**
     * Creates a clone with modified price.
     *
     * @param newPrice the new price per kWh
     * @return a new EnergyOffer with the specified price
     */
    public EnergyOffer cloneWithNewPrice(double newPrice) {
        EnergyOffer clone = this.clone();
        clone.setPrice(newPrice);
        return clone;
    }

    /**
     * Creates a clone with modified quantity.
     *
     * @param newKwh the new amount of energy in kWh
     * @return a new EnergyOffer with the specified quantity
     */
    public EnergyOffer cloneWithNewKwh(double newKwh) {
        EnergyOffer clone = this.clone();
        clone.setKwh(newKwh);
        return clone;
    }

    /**
     * Creates a clone with a different sale type.
     *
     * @param newSaleType the new sale type
     * @return a new EnergyOffer with the specified sale type
     */
    public EnergyOffer cloneWithNewSaleType(SaleType newSaleType) {
        EnergyOffer clone = this.clone();
        clone.setSaleType(newSaleType);
        return clone;
    }

    @Override
    public String toString() {
        return "EnergyOffer [id=" + id + ", producer=" + producer + ", kwh=" + kwh + ", price=" + price + ", saleType="
                + saleType + "]";
    }

}

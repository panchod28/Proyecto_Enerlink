// AuctionRequest.java
package com.enerlink.enerlink.energia.infraestructura.controlador;

public class AuctionRequest {
    private Long buyerId;
    private double amount;
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
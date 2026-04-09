package com.enerlink.enerlink.energia.dominio.decorador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;

public class AuditingTransactionDecorator extends TransactionDecorator {

    private final List<String> auditLog;
    private final DateTimeFormatter formatter;

    public AuditingTransactionDecorator(TransactionComponent wrappedComponent) {
        super(wrappedComponent);
        this.auditLog = new ArrayList<>();
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    @Override
    public Long getId() {
        Long id = super.getId();
        logAudit("GET_ID", "Retrieved transaction ID: " + id);
        return id;
    }

    @Override
    public double getTotalAmount() {
        double amount = super.getTotalAmount();
        logAudit("GET_TOTAL_AMOUNT", "Retrieved total amount: " + amount);
        return amount;
    }

    @Override
    public double getPrice() {
        double price = super.getPrice();
        logAudit("GET_PRICE", "Retrieved price per kWh: " + price);
        return price;
    }

    @Override
    public double getKwh() {
        double kwh = super.getKwh();
        logAudit("GET_KWH", "Retrieved kWh: " + kwh);
        return kwh;
    }

    private void logAudit(String action, String details) {
        String entry = String.format("[%s] %s: %s",
            LocalDateTime.now().format(formatter),
            action,
            details);
        auditLog.add(entry);
    }

    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    public int getAuditLogSize() {
        return auditLog.size();
    }
}

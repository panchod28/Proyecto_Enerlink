package com.enerlink.enerlink.energia.dominio.puerto;

import java.util.List;
import java.util.Optional;

import com.enerlink.enerlink.energia.dominio.modelo.Transaction;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> findAll();

    Optional<Transaction> findById(Long id);

    List<Transaction> findBySellerId(Long sellerId);

    List<Transaction> findByBuyerId(Long buyerId);
}
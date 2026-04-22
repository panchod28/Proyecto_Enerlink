package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository
                extends JpaRepository<TransactionEntity, Long> {
        List<TransactionEntity> findBySellerId(Long sellerId);

        List<TransactionEntity> findByBuyerId(Long buyerId);
}
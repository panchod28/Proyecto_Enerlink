package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enerlink.enerlink.energia.dominio.modelo.SaleType;

public interface EnergyOfferJpaRepository
        extends JpaRepository<EnergyOfferEntity, Long> {

    List<EnergyOfferEntity> findByProducerIdAndAvailableTrue(Long producerId);

    long countByAvailableTrueAndSaleType(SaleType saleType);
}

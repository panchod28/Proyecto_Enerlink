package com.enerlink.enerlink.energia.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyOfferJpaRepository
        extends JpaRepository<EnergyOfferEntity, Long> {
}

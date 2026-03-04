package com.enerlink.enerlink.energia.dominio.puerto;

import java.util.List;
import java.util.Optional;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;

public interface EnergyOfferRepositoryPort {

    EnergyOffer save(EnergyOffer offer);

    List<EnergyOffer> findAll();

    Optional<EnergyOffer> findById(Long id);

    void deleteById(Long id);
}

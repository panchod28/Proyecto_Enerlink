package com.enerlink.enerlink.energia.dominio.puerto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;

public interface EnergyOfferRepositoryPort {

    EnergyOffer save(EnergyOffer offer);

    List<EnergyOffer> findAll();

    Page<EnergyOffer> findAll(Pageable pageable);

    Page<EnergyOffer> findWithFilters(
        SaleType saleType,
        Double minPrice,
        Double maxPrice,
        Double minKwh,
        Double maxKwh,
        Pageable pageable);

    List<EnergyOffer> findByProducerId(Long producerId);

    Optional<EnergyOffer> findById(Long id);

    void deleteById(Long id);

    long countByAvailableTrueAndSaleType(SaleType saleType);
}
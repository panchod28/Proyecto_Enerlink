package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.enerlink.enerlink.energia.dominio.modelo.SaleType;

public interface EnergyOfferJpaRepository
        extends JpaRepository<EnergyOfferEntity, Long> {

    List<EnergyOfferEntity> findByProducerIdAndAvailableTrue(Long producerId);

    long countByAvailableTrueAndSaleType(SaleType saleType);

    @Query("SELECT e FROM EnergyOfferEntity e WHERE e.available = true " +
           "AND (:saleType IS NULL OR e.saleType = :saleType) " +
           "AND (:minPrice IS NULL OR e.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR e.price <= :maxPrice) " +
           "AND (:minKwh IS NULL OR e.kwh >= :minKwh) " +
           "AND (:maxKwh IS NULL OR e.kwh <= :maxKwh)")
    Page<EnergyOfferEntity> findWithFilters(
        @Param("saleType") SaleType saleType,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("minKwh") Double minKwh,
        @Param("maxKwh") Double maxKwh,
        Pageable pageable);
}

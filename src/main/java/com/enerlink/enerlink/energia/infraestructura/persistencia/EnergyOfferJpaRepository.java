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

    @Query(value = """
        SELECT
            eo.sale_type,
            COUNT(DISTINCT eo.id)                        AS total,
            COUNT(DISTINCT t.id)                         AS sold,
            COUNT(DISTINCT eo.id) - COUNT(DISTINCT t.id) AS active,
            COALESCE(AVG(t.price), 0)                    AS avgPrice,
            COALESCE(AVG(eo.kwh), 0)                     AS avgKwh
        FROM energy_offer eo
        LEFT JOIN transactions t ON t.offer_id = eo.id
            AND (:startDate IS NULL OR t.timestamp >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD'))
            AND (:endDate   IS NULL OR t.timestamp <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD') + INTERVAL '1 day')
        WHERE (:saleType IS NULL OR eo.sale_type = :saleType)
        GROUP BY eo.sale_type
        """, nativeQuery = true)
    List<Object[]> findMarketDistribution(
        @Param("startDate") String startDate,
        @Param("endDate")   String endDate,
        @Param("saleType")  String saleType);

        @Query(value = """
        SELECT
            TO_CHAR(DATE_TRUNC('week', t.timestamp), 'YYYY-MM-DD') AS week,
            eo.sale_type                                            AS saleType,
            AVG(t.price)                                           AS avgPrice,
            COUNT(t.id)                                            AS transactions,
            SUM(t.kwh)                                             AS totalKwh
        FROM transactions t
        JOIN energy_offer eo ON eo.id = t.offer_id
        WHERE (:startDate IS NULL OR t.timestamp >= TO_TIMESTAMP(:startDate, 'YYYY-MM-DD'))
          AND (:endDate   IS NULL OR t.timestamp <= TO_TIMESTAMP(:endDate, 'YYYY-MM-DD') + INTERVAL '1 day')
          AND (:saleType  IS NULL OR eo.sale_type = :saleType)
        GROUP BY 1, 2
        HAVING (:minAvgPrice IS NULL OR AVG(t.price) >= :minAvgPrice)
        ORDER BY 1 ASC
        """, nativeQuery = true)
    List<Object[]> findWeeklyPriceTrend(
        @Param("startDate")   String startDate,
        @Param("endDate")     String endDate,
        @Param("saleType")    String saleType,
        @Param("minAvgPrice") Double minAvgPrice);

    @Query(value = """
        SELECT
            eo.id                           AS offerId,
            u.nombre                        AS producerName,
            u.rol                           AS producerRole,
            eo.sale_type                    AS saleType,
            eo.kwh                          AS kwh,
            eo.price                        AS price,
            eo.kwh * eo.price               AS totalValue,
            eo.created_at                   AS createdAt
        FROM energy_offer eo
        JOIN users u ON u.id = eo.producer_id
        WHERE eo.available = true
          AND (:saleType IS NULL OR eo.sale_type = :saleType)
          AND (:minPrice IS NULL OR eo.price >= :minPrice)
          AND (:maxPrice IS NULL OR eo.price <= :maxPrice)
        ORDER BY eo.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findActiveOffersForMonitoring(
        @Param("saleType")  String saleType,
        @Param("minPrice")  Double minPrice,
        @Param("maxPrice")  Double maxPrice);

    @Query(value = """
        SELECT
            eo.id                              AS offerId,
            u.nombre                           AS producerName,
            u.rol                              AS producerRole,
            eo.sale_type                       AS saleType,
            eo.kwh                             AS kwh,
            eo.price                           AS basePrice,
            t.price                            AS soldPrice,
            t.kwh * t.price                    AS totalValue,
            t.commission                       AS commission,
            t.timestamp                        AS soldAt,
            buyer.nombre                       AS buyerName
        FROM energy_offer eo
        JOIN transactions t   ON t.offer_id  = eo.id
        JOIN users u          ON u.id        = eo.producer_id
        JOIN users buyer      ON buyer.id    = t.buyer_id
        WHERE eo.available = false
          AND (:saleType IS NULL OR eo.sale_type = :saleType)
          AND (:minPrice IS NULL OR t.price >= :minPrice)
          AND (:maxPrice IS NULL OR t.price <= :maxPrice)
        ORDER BY t.timestamp DESC
        """, nativeQuery = true)
    List<Object[]> findSoldOffersForMonitoring(
        @Param("saleType")  String saleType,
        @Param("minPrice")  Double minPrice,
        @Param("maxPrice")  Double maxPrice);
}

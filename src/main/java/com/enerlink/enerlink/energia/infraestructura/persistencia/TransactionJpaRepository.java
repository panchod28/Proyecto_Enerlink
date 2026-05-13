package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository
                extends JpaRepository<TransactionEntity, Long> {
        List<TransactionEntity> findBySellerId(Long sellerId);

        List<TransactionEntity> findByBuyerId(Long buyerId);

        @Query(value = """
            SELECT
                CASE
                    WHEN :groupBy = 'day'   THEN TO_CHAR(t.timestamp, 'YYYY-MM-DD')
                    WHEN :groupBy = 'week'  THEN TO_CHAR(DATE_TRUNC('week', t.timestamp), 'YYYY-MM-DD')
                    WHEN :groupBy = 'month' THEN TO_CHAR(t.timestamp, 'YYYY-MM')
                END                     AS period,
                eo.sale_type            AS saleType,
                SUM(t.kwh)              AS totalKwh,
                COUNT(t.id)             AS transactionCount,
                AVG(t.kwh)              AS avgKwh,
                SUM(t.kwh * t.price)    AS totalValue
            FROM transactions t
            JOIN energy_offer eo ON eo.id = t.offer_id
            GROUP BY 1, 2
            ORDER BY 1 ASC, 2
            """, nativeQuery = true)
        List<Object[]> findVolumeByPeriodAndType(@Param("groupBy") String groupBy);

        @Query(value = """
            SELECT buyer_id AS userId, SUM(kwh) AS kwhBought, COUNT(*) AS purchaseCount
            FROM transactions
            GROUP BY buyer_id
            """, nativeQuery = true)
        List<Object[]> findKwhBoughtPerUser();

        @Query(value = """
            SELECT seller_id AS userId, SUM(kwh) AS kwhSold, COUNT(*) AS saleCount
            FROM transactions
            GROUP BY seller_id
            """, nativeQuery = true)
        List<Object[]> findKwhSoldPerUser();

        @Query(value = """
            SELECT
                CASE
                    WHEN :groupBy = 'day'   THEN TO_CHAR(timestamp, 'YYYY-MM-DD')
                    WHEN :groupBy = 'week'  THEN TO_CHAR(DATE_TRUNC('week', timestamp), 'YYYY-MM-DD')
                    WHEN :groupBy = 'month' THEN TO_CHAR(timestamp, 'YYYY-MM')
                END          AS period,
                SUM(commission)     AS totalCommission,
                COUNT(*)            AS transactionCount,
                AVG(commission)     AS avgCommission,
                SUM(kwh * price)    AS totalVolume
            FROM transactions
            GROUP BY 1
            ORDER BY 1 DESC
            """, nativeQuery = true)
        List<Object[]> findCommissionsByPeriod(@Param("groupBy") String groupBy);

        @Query(value = """
            SELECT
                u.nombre                                                AS producerName,
                COUNT(DISTINCT eo.id)                                  AS totalOffers,
                COUNT(DISTINCT t.id)                                   AS soldOffers,
                COALESCE(AVG(t.price), 0)                              AS avgPriceSold,
                COALESCE(AVG(eo.price), 0)                             AS avgPriceBase,
                COALESCE(AVG(
                    EXTRACT(EPOCH FROM (t.timestamp - eo.created_at)) / 86400.0
                ), 0)                                                  AS avgDaysToSell
            FROM users u
            JOIN energy_offer eo ON eo.producer_id = u.id
            LEFT JOIN transactions t ON t.seller_id = u.id
                AND t.offer_id = eo.id
            WHERE u.rol IN ('PRODUCER', 'MIXED')
              AND eo.created_at IS NOT NULL
            GROUP BY u.id, u.nombre
            HAVING COUNT(DISTINCT eo.id) > 0
            ORDER BY COUNT(DISTINCT t.id) DESC
            """, nativeQuery = true)
        List<Object[]> findProducerEfficiencyData();

        @Query(value = """
            SELECT
                u.nombre                                            AS buyerName,
                u.rol                                              AS role,
                COUNT(t.id)                                        AS totalTransactions,
                SUM(t.kwh)                                         AS totalKwhBought,
                SUM(t.kwh * t.price)                               AS totalSpent,
                AVG(t.kwh * t.price)                               AS avgSpentPerTx,
                COUNT(t.id) FILTER (WHERE eo.sale_type = 'DIRECT') AS directCount,
                COUNT(t.id) FILTER (WHERE eo.sale_type = 'AUCTION') AS auctionCount,
                AVG(t.price)                                       AS avgPricePerKwh
            FROM transactions t
            JOIN users u ON u.id = t.buyer_id
            JOIN energy_offer eo ON eo.id = t.offer_id
            GROUP BY u.id, u.nombre, u.rol
            ORDER BY SUM(t.kwh * t.price) DESC
            """, nativeQuery = true)
        List<Object[]> findBuyerActivityData();
}
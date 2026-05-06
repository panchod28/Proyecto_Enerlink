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
}
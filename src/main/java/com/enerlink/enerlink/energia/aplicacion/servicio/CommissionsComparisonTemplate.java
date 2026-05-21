package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

import com.enerlink.enerlink.energia.infraestructura.persistencia.EnergyOfferJpaRepository;
import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;

/**
 * TEMPLATE METHOD PATTERN — ConcreteClass A
 * Dimension: Commissions
 * Variable steps: fetches commission data, calculates commission KPIs.
 * Fixed steps inherited: calculateVariation(), buildResult().
 */
public class CommissionsComparisonTemplate extends ComparativeReportTemplate {

    public CommissionsComparisonTemplate(
            TransactionJpaRepository transactionRepository,
            EnergyOfferJpaRepository offerRepository) {
        super(transactionRepository, offerRepository);
    }

    @Override
    protected String getDimension() { return "COMMISSIONS"; }

    @Override
    protected String[] getKpiKeys() {
        return new String[]{
            "totalCommission", "transactionCount", "avgCommission", "totalVolume"
        };
    }

    @Override
    protected Map<String, Object> fetchPeriodData(String startDate, String endDate) {
        Object raw = transactionRepository.findMetricsByPeriod(startDate, endDate);
        Map<String, Object> data = new LinkedHashMap<>();
        if (raw != null) {
            Object[] row = raw instanceof Object[] && ((Object[]) raw)[0] instanceof Object[]
                ? (Object[]) ((Object[]) raw)[0]
                : (Object[]) raw;
            data.put("transactionCount", ((Number) row[0]).longValue());
            data.put("totalKwh",         round(((Number) row[1]).doubleValue()));
            data.put("totalValue",       round(((Number) row[2]).doubleValue()));
            data.put("totalCommission",  round(((Number) row[3]).doubleValue()));
            data.put("avgPrice",         round(((Number) row[4]).doubleValue()));
            data.put("avgValue",         round(((Number) row[5]).doubleValue()));
        }
        return data;
    }

    @Override
    protected Map<String, Object> calculateKpis(Map<String, Object> rawData) {
        Map<String, Object> kpis = new LinkedHashMap<>();
        long   txCount        = rawData.containsKey("transactionCount")
            ? ((Number) rawData.get("transactionCount")).longValue() : 0;
        double totalCommission = rawData.containsKey("totalCommission")
            ? ((Number) rawData.get("totalCommission")).doubleValue() : 0;
        double totalVolume     = rawData.containsKey("totalValue")
            ? ((Number) rawData.get("totalValue")).doubleValue() : 0;
        double avgCommission   = txCount > 0 ? totalCommission / txCount : 0;

        kpis.put("totalCommission", round(totalCommission));
        kpis.put("transactionCount", txCount);
        kpis.put("avgCommission",   round(avgCommission));
        kpis.put("totalVolume",     round(totalVolume));
        kpis.put("feeRate",         2.0);
        return kpis;
    }
}

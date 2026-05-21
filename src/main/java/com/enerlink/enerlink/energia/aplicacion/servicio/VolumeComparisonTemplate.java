package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

import com.enerlink.enerlink.energia.infraestructura.persistencia.EnergyOfferJpaRepository;
import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;

/**
 * TEMPLATE METHOD PATTERN — ConcreteClass B
 * Dimension: Energy Volume
 * Variable steps: fetches volume data, calculates kWh KPIs.
 * Fixed steps inherited: calculateVariation(), buildResult().
 */
public class VolumeComparisonTemplate extends ComparativeReportTemplate {

    public VolumeComparisonTemplate(
            TransactionJpaRepository transactionRepository,
            EnergyOfferJpaRepository offerRepository) {
        super(transactionRepository, offerRepository);
    }

    @Override
    protected String getDimension() { return "VOLUME"; }

    @Override
    protected String[] getKpiKeys() {
        return new String[]{
            "totalKwh", "transactionCount", "avgKwhPerTx", "totalValue"
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
        long   txCount    = rawData.containsKey("transactionCount")
            ? ((Number) rawData.get("transactionCount")).longValue() : 0;
        double totalKwh   = rawData.containsKey("totalKwh")
            ? ((Number) rawData.get("totalKwh")).doubleValue() : 0;
        double totalValue = rawData.containsKey("totalValue")
            ? ((Number) rawData.get("totalValue")).doubleValue() : 0;
        double avgKwh     = txCount > 0 ? totalKwh / txCount : 0;

        kpis.put("totalKwh",        round(totalKwh));
        kpis.put("transactionCount", txCount);
        kpis.put("avgKwhPerTx",     round(avgKwh));
        kpis.put("totalValue",      round(totalValue));
        return kpis;
    }
}

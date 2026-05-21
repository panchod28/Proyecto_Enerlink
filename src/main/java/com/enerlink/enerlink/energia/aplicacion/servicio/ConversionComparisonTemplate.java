package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

import com.enerlink.enerlink.energia.infraestructura.persistencia.EnergyOfferJpaRepository;
import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;

/**
 * TEMPLATE METHOD PATTERN — ConcreteClass C
 * Dimension: Market Conversion
 * Variable steps: fetches offer+transaction data, calculates conversion KPIs.
 * Fixed steps inherited: calculateVariation(), buildResult().
 */
public class ConversionComparisonTemplate extends ComparativeReportTemplate {

    public ConversionComparisonTemplate(
            TransactionJpaRepository transactionRepository,
            EnergyOfferJpaRepository offerRepository) {
        super(transactionRepository, offerRepository);
    }

    @Override
    protected String getDimension() { return "CONVERSION"; }

    @Override
    protected String[] getKpiKeys() {
        return new String[]{
            "conversionRate", "totalOffers", "soldOffers", "avgSoldPrice"
        };
    }

    @Override
    protected Map<String, Object> fetchPeriodData(String startDate, String endDate) {
        Object raw = transactionRepository.findConversionByPeriod(startDate, endDate);
        Map<String, Object> data = new LinkedHashMap<>();
        if (raw != null) {
            Object[] row = raw instanceof Object[] && ((Object[]) raw)[0] instanceof Object[]
                ? (Object[]) ((Object[]) raw)[0]
                : (Object[]) raw;
            data.put("totalOffers",   ((Number) row[0]).longValue());
            data.put("soldOffers",    ((Number) row[1]).longValue());
            data.put("avgSoldPrice",  round(((Number) row[2]).doubleValue()));
            data.put("avgBasePrice",  round(((Number) row[3]).doubleValue()));
            data.put("directOffers",  ((Number) row[4]).longValue());
            data.put("auctionOffers", ((Number) row[5]).longValue());
        }
        return data;
    }

    @Override
    protected Map<String, Object> calculateKpis(Map<String, Object> rawData) {
        Map<String, Object> kpis = new LinkedHashMap<>();
        long   totalOffers  = rawData.containsKey("totalOffers")
            ? ((Number) rawData.get("totalOffers")).longValue() : 0;
        long   soldOffers   = rawData.containsKey("soldOffers")
            ? ((Number) rawData.get("soldOffers")).longValue() : 0;
        double avgSoldPrice = rawData.containsKey("avgSoldPrice")
            ? ((Number) rawData.get("avgSoldPrice")).doubleValue() : 0;
        double avgBasePrice = rawData.containsKey("avgBasePrice")
            ? ((Number) rawData.get("avgBasePrice")).doubleValue() : 0;
        double convRate     = totalOffers > 0
            ? Math.round((double) soldOffers / totalOffers * 100 * 100.0) / 100.0 : 0;
        double pricePerf    = avgBasePrice > 0
            ? Math.round((avgSoldPrice / avgBasePrice - 1) * 100 * 100.0) / 100.0 : 0;

        kpis.put("totalOffers",    totalOffers);
        kpis.put("soldOffers",     soldOffers);
        kpis.put("conversionRate", convRate);
        kpis.put("avgSoldPrice",   round(avgSoldPrice));
        kpis.put("avgBasePrice",   round(avgBasePrice));
        kpis.put("pricePerformance", pricePerf);
        return kpis;
    }
}

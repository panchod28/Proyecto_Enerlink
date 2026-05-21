package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.LinkedHashMap;
import java.util.Map;

import com.enerlink.enerlink.energia.infraestructura.persistencia.EnergyOfferJpaRepository;
import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;

/**
 * TEMPLATE METHOD PATTERN — Abstract Class
 *
 * Defines the fixed skeleton of the comparative report algorithm.
 * Subclasses override only the variable steps (fetchPeriodData, calculateKpis)
 * without changing the overall structure defined in generate().
 *
 * Fixed steps (same for all dimensions):
 *   1. fetchPeriodData()    ← ABSTRACT — each dimension fetches different data
 *   2. calculateKpis()      ← ABSTRACT — each dimension calculates different KPIs
 *   3. calculateVariation() ← CONCRETE — same formula for all dimensions
 *   4. buildResult()        ← CONCRETE — same output structure for all dimensions
 *
 * Concrete subclasses:
 *   - CommissionsComparisonTemplate
 *   - VolumeComparisonTemplate
 *   - ConversionComparisonTemplate
 */
public abstract class ComparativeReportTemplate {

    protected final TransactionJpaRepository  transactionRepository;
    protected final EnergyOfferJpaRepository  offerRepository;

    protected String periodAStart;
    protected String periodAEnd;
    protected String periodBStart;
    protected String periodBEnd;

    protected ComparativeReportTemplate(
            TransactionJpaRepository transactionRepository,
            EnergyOfferJpaRepository offerRepository) {
        this.transactionRepository = transactionRepository;
        this.offerRepository       = offerRepository;
    }

    /**
     * TEMPLATE METHOD — Fixed skeleton.
     * This method defines the algorithm structure.
     * Subclasses CANNOT override this method.
     */
    public final Map<String, Object> generate(
            String periodAStart, String periodAEnd,
            String periodBStart, String periodBEnd) {

        this.periodAStart = periodAStart;
        this.periodAEnd   = periodAEnd;
        this.periodBStart = periodBStart;
        this.periodBEnd   = periodBEnd;

        Map<String, Object> dataA = fetchPeriodData(periodAStart, periodAEnd);
        Map<String, Object> dataB = fetchPeriodData(periodBStart, periodBEnd);

        Map<String, Object> kpisA = calculateKpis(dataA);
        Map<String, Object> kpisB = calculateKpis(dataB);

        Map<String, Object> variation = calculateVariation(kpisA, kpisB);

        return buildResult(kpisA, kpisB, variation);
    }

    /**
     * ABSTRACT STEP 1 — Fetch raw data for a specific period.
     * Each subclass fetches the data relevant to its dimension.
     */
    protected abstract Map<String, Object> fetchPeriodData(
        String startDate, String endDate);

    /**
     * ABSTRACT STEP 2 — Calculate dimension-specific KPIs from raw data.
     * Each subclass computes the metrics relevant to its dimension.
     */
    protected abstract Map<String, Object> calculateKpis(
        Map<String, Object> rawData);

    /**
     * ABSTRACT: returns the dimension name for the response.
     */
    protected abstract String getDimension();

    /**
     * ABSTRACT: returns the list of KPI keys for variation calculation.
     */
    protected abstract String[] getKpiKeys();

    /**
     * CONCRETE STEP 3 — Calculate percentage variation between periods.
     * Formula is identical for all dimensions: (B - A) / A * 100
     * This step is the same regardless of the dimension being compared.
     */
    protected final Map<String, Object> calculateVariation(
            Map<String, Object> kpisA,
            Map<String, Object> kpisB) {

        Map<String, Object> variation = new LinkedHashMap<>();
        for (String key : getKpiKeys()) {
            double a = kpisA.containsKey(key)
                ? ((Number) kpisA.get(key)).doubleValue() : 0;
            double b = kpisB.containsKey(key)
                ? ((Number) kpisB.get(key)).doubleValue() : 0;
            double pct = a > 0 ? Math.round((b - a) / a * 100 * 100.0) / 100.0 : 0;
            variation.put(key + "Change", pct);
            variation.put(key + "Trend",  pct > 0 ? "UP" : pct < 0 ? "DOWN" : "STABLE");
        }
        return variation;
    }

    /**
     * CONCRETE STEP 4 — Assemble the final response map.
     * Output structure is identical for all dimensions.
     */
    protected final Map<String, Object> buildResult(
            Map<String, Object> kpisA,
            Map<String, Object> kpisB,
            Map<String, Object> variation) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dimension",   getDimension());
        result.put("periodA", Map.of(
            "start", periodAStart, "end", periodAEnd, "kpis", kpisA));
        result.put("periodB", Map.of(
            "start", periodBStart, "end", periodBEnd, "kpis", kpisB));
        result.put("variation", variation);
        return result;
    }

    protected double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.enerlink.enerlink.energia.infraestructura.persistencia.EnergyOfferJpaRepository;
import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;
import com.enerlink.enerlink.iot.infraestructura.persistencia.IoTDeviceJpaRepository;
import com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository;

@Service
public class ReportService {

    private final TransactionJpaRepository  transactionRepository;
    private final IoTDeviceJpaRepository    iotDeviceRepository;
    private final UserJpaRepository         userRepository;
    private final EnergyOfferJpaRepository  offerJpaRepository;

    public ReportService(
            TransactionJpaRepository transactionRepository,
            IoTDeviceJpaRepository   iotDeviceRepository,
            UserJpaRepository        userRepository,
            EnergyOfferJpaRepository offerJpaRepository) {
        this.transactionRepository = transactionRepository;
        this.iotDeviceRepository   = iotDeviceRepository;
        this.userRepository        = userRepository;
        this.offerJpaRepository    = offerJpaRepository;
    }

    // ─── REPORT 4: Energy Volume ────────────────────────────────────

    public Map<String, Object> getEnergyVolume(String groupBy, int page, int size) {
        List<Object[]> rows = transactionRepository.findVolumeByPeriodAndType(groupBy);

        Map<String, Map<String, Object>> byPeriod = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String period   = (String) row[0];
            String saleType = (String) row[1];
            double kwh      = ((Number) row[2]).doubleValue();
            long   count    = ((Number) row[3]).longValue();
            double value    = ((Number) row[5]).doubleValue();

            byPeriod.computeIfAbsent(period, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("period",        k);
                m.put("DIRECT_kwh",    0.0);
                m.put("DIRECT_count",  0L);
                m.put("DIRECT_value",  0.0);
                m.put("AUCTION_kwh",   0.0);
                m.put("AUCTION_count", 0L);
                m.put("AUCTION_value", 0.0);
                return m;
            });
            Map<String, Object> p = byPeriod.get(period);
            p.put(saleType + "_kwh",   round(kwh));
            p.put(saleType + "_count", count);
            p.put(saleType + "_value", round(value));
        }

        List<Map<String, Object>> breakdown = new ArrayList<>(byPeriod.values());

        double totalKwh   = rows.stream().mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();
        long   totalCount = rows.stream().mapToLong(r  -> ((Number) r[3]).longValue()).sum();
        double totalValue = rows.stream().mapToDouble(r -> ((Number) r[5]).doubleValue()).sum();
        double avgPerTx   = totalCount > 0 ? totalKwh / totalCount : 0;
        double directKwh  = rows.stream()
            .filter(r -> "DIRECT".equals(r[1]))
            .mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();
        double auctionKwh = rows.stream()
            .filter(r -> "AUCTION".equals(r[1]))
            .mapToDouble(r -> ((Number) r[2]).doubleValue()).sum();

        int totalElements = breakdown.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIndex     = Math.min(page * size, totalElements);
        int toIndex       = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> pagedBreakdown = breakdown.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupBy",       groupBy);
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("kpis", Map.of(
            "totalKwh",    round(totalKwh),
            "totalValue",  round(totalValue),
            "totalTx",     totalCount,
            "avgKwhPerTx", round(avgPerTx),
            "directKwh",   round(directKwh),
            "auctionKwh",  round(auctionKwh),
            "directShare", totalKwh > 0
                ? Math.round(directKwh / totalKwh * 100 * 10.0) / 10.0 : 0.0
        ));
        result.put("breakdown", pagedBreakdown);
        return result;
    }

    // ─── REPORT 1: Commissions ──────────────────────────────────────

    public Map<String, Object> getCommissions(String groupBy, int page, int size) {
        List<Object[]> rows = transactionRepository.findCommissionsByPeriod(groupBy);

        List<Map<String, Object>> breakdown = rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("period",           row[0]);
            m.put("totalCommission",  round(((Number) row[1]).doubleValue()));
            m.put("transactionCount", ((Number) row[2]).longValue());
            m.put("avgCommission",    round(((Number) row[3]).doubleValue()));
            m.put("totalVolume",      round(((Number) row[4]).doubleValue()));
            return m;
        }).collect(Collectors.toList());

        double grandTotal = breakdown.stream()
            .mapToDouble(r -> ((Number) r.get("totalCommission")).doubleValue())
            .sum();
        long totalTx = breakdown.stream()
            .mapToLong(r -> ((Number) r.get("transactionCount")).longValue())
            .sum();
        double totalVolume = breakdown.stream()
            .mapToDouble(r -> ((Number) r.get("totalVolume")).doubleValue())
            .sum();

        int totalElements = breakdown.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIndex     = Math.min(page * size, totalElements);
        int toIndex       = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> pagedBreakdown = breakdown.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groupBy",       groupBy);
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("grandTotal",    round(grandTotal));
        result.put("totalTx",       totalTx);
        result.put("totalVolume",   round(totalVolume));
        result.put("feeRate",       2.0);
        result.put("breakdown",     pagedBreakdown);
        return result;
    }

    // ─── REPORT 3: Market Summary ───────────────────────────────────

    public Map<String, Object> getMarketSummary(
            String startDate, String endDate,
            String saleType, Double minAvgPrice) {

        List<Object[]> distribution = offerJpaRepository.findMarketDistribution(startDate, endDate, saleType);
        List<Map<String, Object>> typeBreakdown = distribution.stream().map(row -> {
            String saleTypeStr = (String) row[0];
            long   total    = ((Number) row[1]).longValue();
            long   sold     = ((Number) row[2]).longValue();
            long   active   = ((Number) row[3]).longValue();
            double avgPrice = ((Number) row[4]).doubleValue();
            double avgKwh   = ((Number) row[5]).doubleValue();
            double convRate = total > 0 ? (double) sold / total * 100 : 0;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("saleType",       saleTypeStr);
            m.put("total",          total);
            m.put("sold",           sold);
            m.put("active",         active);
            m.put("conversionRate", Math.round(convRate * 100.0) / 100.0);
            m.put("avgPrice",       round(avgPrice));
            m.put("avgKwh",         round(avgKwh));
            return m;
        }).collect(Collectors.toList());

        long totalOffers = typeBreakdown.stream()
            .mapToLong(r -> ((Number) r.get("total")).longValue()).sum();
        long totalSold = typeBreakdown.stream()
            .mapToLong(r -> ((Number) r.get("sold")).longValue()).sum();
        double globalConversion = totalOffers > 0
            ? Math.round((double) totalSold / totalOffers * 100 * 100.0) / 100.0 : 0;

        List<Object[]> trendRows = offerJpaRepository.findWeeklyPriceTrend(startDate, endDate, saleType, minAvgPrice);
        List<Map<String, Object>> weeklyTrend = trendRows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("week",         row[0]);
            m.put("saleType",     row[1]);
            m.put("avgPrice",     round(((Number) row[2]).doubleValue()));
            m.put("transactions", ((Number) row[3]).longValue());
            m.put("totalKwh",     round(((Number) row[4]).doubleValue()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filters", Map.of(
            "startDate",   startDate   != null ? startDate   : "",
            "endDate",     endDate     != null ? endDate     : "",
            "saleType",    saleType    != null ? saleType    : "",
            "minAvgPrice", minAvgPrice != null ? minAvgPrice : 0.0
        ));
        result.put("kpis", Map.of(
            "totalOffers",      totalOffers,
            "totalSold",        totalSold,
            "totalActive",      totalOffers - totalSold,
            "globalConversion", globalConversion
        ));
        result.put("typeBreakdown", typeBreakdown);
        result.put("weeklyTrend",   weeklyTrend);
        return result;
    }

    // ─── REPORT 2: Producer Efficiency ──────────────────────────────

    public Map<String, Object> getProducerEfficiency(int page, int size) {
        List<Object[]> rows = transactionRepository.findProducerEfficiencyData();

        List<Map<String, Object>> producers = rows.stream().map(row -> {
            String producerName  = (String)  row[0];
            long   totalOffers   = ((Number) row[1]).longValue();
            long   soldOffers    = ((Number) row[2]).longValue();
            double avgPriceSold  = ((Number) row[3]).doubleValue();
            double avgPriceBase  = ((Number) row[4]).doubleValue();
            double avgDaysToSell = ((Number) row[5]).doubleValue();

            double conversionRate   = totalOffers > 0
                ? (double) soldOffers / totalOffers * 100 : 0;
            double pricePerformance = avgPriceBase > 0
                ? (avgPriceSold / avgPriceBase - 1) * 100 : 0;
            double speedScore = avgDaysToSell > 0
                ? Math.max(0, 100 - avgDaysToSell * 5) : 100;

            double normalizedPrice = Math.min(Math.max(pricePerformance + 100, 0), 200);
            double score = (conversionRate * 0.4)
                         + (normalizedPrice * 0.2)
                         + (speedScore * 0.4);

            String classification = score >= 70 ? "EFICIENTE"
                : score >= 40 ? "PROMEDIO" : "INEFICIENTE";

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("producerName",     producerName);
            m.put("totalOffers",      totalOffers);
            m.put("soldOffers",       soldOffers);
            m.put("conversionRate",   Math.round(conversionRate * 100.0) / 100.0);
            m.put("avgPriceSold",     round(avgPriceSold));
            m.put("avgPriceBase",     round(avgPriceBase));
            m.put("pricePerformance", Math.round(pricePerformance * 100.0) / 100.0);
            m.put("avgDaysToSell",    Math.round(avgDaysToSell * 10.0) / 10.0);
            m.put("score",            Math.round(score * 10.0) / 10.0);
            m.put("classification",   classification);
            return m;
        })
        .sorted((a, b) -> Double.compare(
            ((Number) b.get("score")).doubleValue(),
            ((Number) a.get("score")).doubleValue()))
        .collect(Collectors.toList());

        long efficient   = producers.stream().filter(p -> "EFICIENTE".equals(p.get("classification"))).count();
        long average     = producers.stream().filter(p -> "PROMEDIO".equals(p.get("classification"))).count();
        long inefficient = producers.stream().filter(p -> "INEFICIENTE".equals(p.get("classification"))).count();

        int totalElements = producers.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIndex     = Math.min(page * size, totalElements);
        int toIndex       = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> pagedProducers = producers.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("summary", Map.of(
            "totalProducers", totalElements,
            "efficient",      efficient,
            "average",        average,
            "inefficient",    inefficient
        ));
        result.put("producers", pagedProducers);
        return result;
    }

    // ─── REPORT 6: Buyer Activity ─────────────────────────────────────

    public Map<String, Object> getBuyerActivity(int page, int size) {
        List<Object[]> rows = transactionRepository.findBuyerActivityData();

        List<Map<String, Object>> buyers = new ArrayList<>();
        double maxSpent = 0;
        long   maxTx    = 0;

        for (Object[] row : rows) {
            String  buyerName        = (String)  row[0];
            String  role             = (String)  row[1];
            long    totalTransactions = ((Number) row[2]).longValue();
            double  totalKwhBought   = ((Number) row[3]).doubleValue();
            double  totalSpent       = ((Number) row[4]).doubleValue();
            double  avgSpentPerTx    = ((Number) row[5]).doubleValue();
            long    directCount      = ((Number) row[6]).longValue();
            long    auctionCount     = ((Number) row[7]).longValue();
            double  avgPricePerKwh   = ((Number) row[8]).doubleValue();

            String classification = totalTransactions >= 5 ? "FRECUENTE"
                : totalTransactions >= 2 ? "OCASIONAL" : "INACTIVO";
            String preference = directCount >= auctionCount ? "DIRECT" : "AUCTION";

            if (totalSpent       > maxSpent) maxSpent = totalSpent;
            if (totalTransactions > maxTx)    maxTx    = totalTransactions;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("buyerName",         buyerName);
            m.put("role",              role);
            m.put("totalTransactions", totalTransactions);
            m.put("totalKwhBought",    round(totalKwhBought));
            m.put("totalSpent",        round(totalSpent));
            m.put("avgSpentPerTx",     round(avgSpentPerTx));
            m.put("avgPricePerKwh",    round(avgPricePerKwh));
            m.put("directCount",       directCount);
            m.put("auctionCount",      auctionCount);
            m.put("preference",        preference);
            m.put("classification",     classification);
            buyers.add(m);
        }

        final double fMaxSpent = maxSpent > 0 ? maxSpent : 1;
        final long   fMaxTx    = maxTx    > 0 ? maxTx    : 1;

        buyers.forEach(b -> {
            double spent = ((Number) b.get("totalSpent")).doubleValue();
            long   tx    = ((Number) b.get("totalTransactions")).longValue();
            double score = (spent / fMaxSpent * 60.0) + ((double) tx / fMaxTx * 40.0);
            b.put("score", Math.round(score * 10.0) / 10.0);
        });

        buyers.sort((a, b) -> Double.compare(
            ((Number) b.get("score")).doubleValue(),
            ((Number) a.get("score")).doubleValue()));

        long frequent   = 0;
        long occasional = 0;
        long inactive   = 0;
        for (Map<String, Object> b : buyers) {
            String c = (String) b.get("classification");
            if      ("FRECUENTE".equals(c)) frequent++;
            else if ("OCASIONAL".equals(c)) occasional++;
            else                            inactive++;
        }

        int totalElements = buyers.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int fromIndex     = Math.min(page * size, totalElements);
        int toIndex       = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> pagedBuyers = buyers.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("summary", Map.of(
            "totalBuyers", totalElements,
            "frequent",    frequent,
            "occasional",  occasional,
            "inactive",    inactive
        ));
        result.put("buyers", pagedBuyers);
        return result;
    }

    // ─── REPORT 7: Offer Monitoring ─────────────────────────────────

    public Map<String, Object> getOfferMonitoring(
            String state,
            String saleType,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        List<Map<String, Object>> items;
        Map<String, Object> summary;
        int totalElements;
        int totalPages;

        if ("COMPARATIVA".equals(state)) {
            List<Object[]> activeRows = offerJpaRepository.findActiveOffersForMonitoring(null, null, null);
            List<Object[]> soldRows   = offerJpaRepository.findSoldOffersForMonitoring(null, null, null);

            double activeValue   = activeRows.stream().mapToDouble(r -> ((Number) r[6]).doubleValue()).sum();
            double activeKwh     = activeRows.stream().mapToDouble(r -> ((Number) r[4]).doubleValue()).sum();
            double activeAvgPrice = activeRows.isEmpty() ? 0
                : activeRows.stream().mapToDouble(r -> ((Number) r[5]).doubleValue()).average().orElse(0);
            int    activeCount   = activeRows.size();

            double soldRevenue    = soldRows.stream().mapToDouble(r -> ((Number) r[7]).doubleValue()).sum();
            double soldAvgPrice   = soldRows.isEmpty() ? 0
                : soldRows.stream().mapToDouble(r -> ((Number) r[6]).doubleValue()).average().orElse(0);
            int    soldCount      = soldRows.size();

            int    totalOffers    = activeCount + soldCount;
            double conversionRate = totalOffers > 0 ? (double) soldCount / totalOffers * 100 : 0;

            summary = new LinkedHashMap<>();
            summary.put("totalOffers",       totalOffers);
            summary.put("activeCount",       activeCount);
            summary.put("soldCount",         soldCount);
            summary.put("conversionRate",    Math.round(conversionRate * 100.0) / 100.0);
            summary.put("totalActiveValue",  round(activeValue));
            summary.put("totalRevenueValue", round(soldRevenue));
            summary.put("avgActivePrice",    round(activeAvgPrice));
            summary.put("avgSoldPrice",      round(soldAvgPrice));

            totalElements = 0;
            totalPages    = 0;
            items         = Collections.emptyList();
        } else if ("VENDIDAS".equals(state)) {
            List<Object[]> rows = offerJpaRepository.findSoldOffersForMonitoring(saleType, minPrice, maxPrice);
            items = new ArrayList<>();
            for (Object[] row : rows) {
                long   offerId      = ((Number) row[0]).longValue();
                String producerName = (String)  row[1];
                String producerRole = (String)  row[2];
                String st           = (String)  row[3];
                double kwh          = ((Number) row[4]).doubleValue();
                double basePrice    = ((Number) row[5]).doubleValue();
                double soldPrice    = ((Number) row[6]).doubleValue();
                double totalValue   = ((Number) row[7]).doubleValue();
                double commission   = ((Number) row[8]).doubleValue();
                String soldAt       = String.valueOf(row[9]);
                String buyerName    = (String)  row[10];

                double perf = basePrice > 0 ? (soldPrice / basePrice - 1) * 100 : 0;

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("offerId",          offerId);
                m.put("producerName",     producerName);
                m.put("producerRole",     producerRole);
                m.put("saleType",         st);
                m.put("kwh",              round(kwh));
                m.put("basePrice",        round(basePrice));
                m.put("soldPrice",        round(soldPrice));
                m.put("totalValue",       round(totalValue));
                m.put("commission",       round(commission));
                m.put("pricePerformance", Math.round(perf * 100.0) / 100.0);
                m.put("soldAt",           soldAt);
                m.put("buyerName",        buyerName);
                items.add(m);
            }

            double totalKwh       = rows.stream().mapToDouble(r -> ((Number) r[4]).doubleValue()).sum();
            double totalRevenue   = rows.stream().mapToDouble(r -> ((Number) r[7]).doubleValue()).sum();
            double totalCommission = rows.stream().mapToDouble(r -> ((Number) r[8]).doubleValue()).sum();
            double avgSoldPrice   = rows.isEmpty() ? 0
                : rows.stream().mapToDouble(r -> ((Number) r[6]).doubleValue()).average().orElse(0);
            long   directCount    = rows.stream().filter(r -> "DIRECT".equals(r[3])).count();
            long   auctionCount   = rows.stream().filter(r -> "AUCTION".equals(r[3])).count();

            summary = new LinkedHashMap<>();
            summary.put("totalSold",       rows.size());
            summary.put("totalKwh",        round(totalKwh));
            summary.put("totalRevenue",    round(totalRevenue));
            summary.put("totalCommission", round(totalCommission));
            summary.put("avgSoldPrice",    round(avgSoldPrice));
            summary.put("directCount",     directCount);
            summary.put("auctionCount",    auctionCount);

            totalElements = items.size();
            totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        } else {
            // default: ACTIVAS
            List<Object[]> rows = offerJpaRepository.findActiveOffersForMonitoring(saleType, minPrice, maxPrice);
            items = new ArrayList<>();
            for (Object[] row : rows) {
                long   offerId      = ((Number) row[0]).longValue();
                String producerName = (String)  row[1];
                String producerRole = (String)  row[2];
                String st           = (String)  row[3];
                double kwh          = ((Number) row[4]).doubleValue();
                double price        = ((Number) row[5]).doubleValue();
                double totalValue   = ((Number) row[6]).doubleValue();
                String createdAt    = String.valueOf(row[7]);

                Map<String, Object> m = new LinkedHashMap<>();
                m.put("offerId",      offerId);
                m.put("producerName", producerName);
                m.put("producerRole", producerRole);
                m.put("saleType",     st);
                m.put("kwh",          round(kwh));
                m.put("price",        round(price));
                m.put("totalValue",   round(totalValue));
                m.put("createdAt",    createdAt);
                items.add(m);
            }

            double totalKwh     = rows.stream().mapToDouble(r -> ((Number) r[4]).doubleValue()).sum();
            double totalValue   = rows.stream().mapToDouble(r -> ((Number) r[6]).doubleValue()).sum();
            double avgPrice     = rows.isEmpty() ? 0
                : rows.stream().mapToDouble(r -> ((Number) r[5]).doubleValue()).average().orElse(0);
            long   directCount  = rows.stream().filter(r -> "DIRECT".equals(r[3])).count();
            long   auctionCount = rows.stream().filter(r -> "AUCTION".equals(r[3])).count();

            summary = new LinkedHashMap<>();
            summary.put("totalActive",  rows.size());
            summary.put("totalKwh",     round(totalKwh));
            summary.put("totalValue",   round(totalValue));
            summary.put("avgPrice",     round(avgPrice));
            summary.put("directCount",  directCount);
            summary.put("auctionCount", auctionCount);

            totalElements = items.size();
            totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        }

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex   = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> pagedItems = totalElements > 0
            ? items.subList(fromIndex, toIndex) : items;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("state",         state);
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("summary",       summary);
        result.put("items",         pagedItems);
        return result;
    }

    // ─── REPORT 5: User Energy Profile ──────────────────────────────

    public Map<String, Object> getUserEnergyProfiles(
            String classification, String role,
            Double minSelfSufficiency, Double maxSelfSufficiency,
            String sortBy, int page, int size) {

        List<Map<String, Object>> profiles = computeUserEnergyProfiles();

        if (classification != null && !classification.isBlank())
            profiles = profiles.stream()
                .filter(p -> classification.equals(p.get("classification")))
                .collect(Collectors.toList());
        if (role != null && !role.isBlank())
            profiles = profiles.stream()
                .filter(p -> role.equals(p.get("role")))
                .collect(Collectors.toList());
        if (minSelfSufficiency != null)
            profiles = profiles.stream()
                .filter(p -> ((Number) p.get("selfSufficiency")).doubleValue() >= minSelfSufficiency)
                .collect(Collectors.toList());
        if (maxSelfSufficiency != null)
            profiles = profiles.stream()
                .filter(p -> ((Number) p.get("selfSufficiency")).doubleValue() <= maxSelfSufficiency)
                .collect(Collectors.toList());

        if (sortBy != null) {
            java.util.Comparator<Map<String, Object>> comparator = switch (sortBy) {
                case "selfSufficiency_asc"  -> java.util.Comparator.comparingDouble(
                    p -> ((Number) p.get("selfSufficiency")).doubleValue());
                case "selfSufficiency_desc" -> java.util.Comparator.comparingDouble(
                    (Map<String, Object> p) -> ((Number) p.get("selfSufficiency")).doubleValue()).reversed();
                case "netBalance_asc"       -> java.util.Comparator.comparingDouble(
                    p -> ((Number) p.get("netBalance")).doubleValue());
                case "netBalance_desc"      -> java.util.Comparator.comparingDouble(
                    (Map<String, Object> p) -> ((Number) p.get("netBalance")).doubleValue()).reversed();
                case "generated_asc"        -> java.util.Comparator.comparingDouble(
                    p -> ((Number) p.get("generated")).doubleValue());
                case "generated_desc"       -> java.util.Comparator.comparingDouble(
                    (Map<String, Object> p) -> ((Number) p.get("generated")).doubleValue()).reversed();
                default -> null;
            };
            if (comparator != null)
                profiles = profiles.stream().sorted(comparator).collect(Collectors.toList());
        }

        int totalElements = profiles.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        long exporters      = profiles.stream().filter(p -> "EXPORTADOR_NETO".equals(p.get("classification"))).count();
        long selfSufficient = profiles.stream().filter(p -> "AUTOSUFICIENTE".equals(p.get("classification"))).count();
        long dependent      = profiles.stream().filter(p -> "DEPENDIENTE".equals(p.get("classification"))).count();

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex   = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> paginatedProfiles = profiles.subList(fromIndex, toIndex);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content",       paginatedProfiles);
        result.put("totalElements", totalElements);
        result.put("totalPages",    totalPages);
        result.put("number",        page);
        result.put("size",          size);
        result.put("last",          page >= totalPages - 1);
        result.put("summary", Map.of(
            "totalUsers",     totalElements,
            "exporters",      exporters,
            "selfSufficient", selfSufficient,
            "dependent",      dependent
        ));
        return result;
    }

    public Map<String, Object> getUserEnergyProfileById(Long userId) {
        return computeUserEnergyProfiles().stream()
            .filter(p -> userId.equals(((Number) p.get("userId")).longValue()))
            .findFirst()
            .orElse(null);
    }

    // ─── Private helpers ─────────────────────────────────────────────

    private List<Map<String, Object>> computeUserEnergyProfiles() {
        Map<Long, Double> generationMap     = new HashMap<>();
        Map<Long, Double> iotConsumptionMap = new HashMap<>();
        Map<Long, Double> boughtMap         = new HashMap<>();
        Map<Long, Double> soldMap           = new HashMap<>();

        iotDeviceRepository.findGenerationByUser().forEach(r ->
            generationMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
        iotDeviceRepository.findConsumptionByUser().forEach(r ->
            iotConsumptionMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
        transactionRepository.findKwhBoughtPerUser().forEach(r ->
            boughtMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));
        transactionRepository.findKwhSoldPerUser().forEach(r ->
            soldMap.put(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()));

        return userRepository.findAll().stream().map(user -> {
            Long   uid         = user.getId();
            double generated   = generationMap.getOrDefault(uid, 0.0);
            double iotConsumed = iotConsumptionMap.getOrDefault(uid, 0.0);
            double bought      = boughtMap.getOrDefault(uid, 0.0);
            double sold        = soldMap.getOrDefault(uid, 0.0);

            double totalDemand  = iotConsumed + sold;
            double netBalance   = generated - iotConsumed - sold + bought;

            double selfSufficiency = totalDemand > 0
                ? Math.min(generated / totalDemand * 100, 100) : 0;
            double exportPotential = Math.max(generated - iotConsumed, 0);

            String classification;
            if (generated > iotConsumed + sold * 0.5)   classification = "EXPORTADOR_NETO";
            else if (selfSufficiency >= 60)              classification = "AUTOSUFICIENTE";
            else                                         classification = "DEPENDIENTE";

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId",          uid);
            m.put("userName",        user.getNombre());
            m.put("role",            user.getRol());
            m.put("generated",       round(generated));
            m.put("iotConsumed",     round(iotConsumed));
            m.put("bought",          round(bought));
            m.put("sold",            round(sold));
            m.put("netBalance",      round(netBalance));
            m.put("selfSufficiency", Math.round(selfSufficiency * 10.0) / 10.0);
            m.put("exportPotential",  round(exportPotential));
            m.put("classification",  classification);
            return m;
        })
        .filter(p -> ((Number) p.get("generated")).doubleValue() > 0
                  || ((Number) p.get("bought")).doubleValue()    > 0
                  || ((Number) p.get("sold")).doubleValue()      > 0)
        .collect(Collectors.toList());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

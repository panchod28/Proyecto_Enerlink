package com.enerlink.enerlink.energia.infraestructura.controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TransactionJpaRepository transactionRepository;

    public ReportController(TransactionJpaRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/volume")
    public ResponseEntity<?> getEnergyVolume(
            @RequestParam(defaultValue = "month") String groupBy) {

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

        return ResponseEntity.ok(Map.of(
            "groupBy", groupBy,
            "kpis", Map.of(
                "totalKwh",    round(totalKwh),
                "totalValue",  round(totalValue),
                "totalTx",     totalCount,
                "avgKwhPerTx", round(avgPerTx),
                "directKwh",   round(directKwh),
                "auctionKwh",  round(auctionKwh),
                "directShare", totalKwh > 0
                    ? Math.round(directKwh / totalKwh * 100 * 10.0) / 10.0 : 0.0
            ),
            "breakdown", breakdown
        ));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

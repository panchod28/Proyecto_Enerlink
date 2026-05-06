package com.enerlink.enerlink.energia.infraestructura.controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.energia.infraestructura.persistencia.TransactionJpaRepository;
import com.enerlink.enerlink.iot.infraestructura.persistencia.IoTDeviceJpaRepository;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TransactionJpaRepository transactionRepository;
    private final IoTDeviceJpaRepository iotDeviceRepository;
    private final com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository userRepository;

    public ReportController(
            TransactionJpaRepository transactionRepository,
            IoTDeviceJpaRepository iotDeviceRepository,
            com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.iotDeviceRepository   = iotDeviceRepository;
        this.userRepository        = userRepository;
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

    @GetMapping("/user-energy-profile")
    public ResponseEntity<?> getUserEnergyProfiles(
            @RequestParam(required = false)              String classification,
            @RequestParam(required = false)              String role,
            @RequestParam(required = false)              Double minSelfSufficiency,
            @RequestParam(required = false)              Double maxSelfSufficiency,
            @RequestParam(required = false)              String sortBy,
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "20")           int size) {

        List<Map<String, Object>> profiles = computeUserEnergyProfiles();

        // Apply filters
        if (classification != null && !classification.isBlank()) {
            profiles = profiles.stream()
                .filter(p -> classification.equals(p.get("classification")))
                .collect(Collectors.toList());
        }
        if (role != null && !role.isBlank()) {
            profiles = profiles.stream()
                .filter(p -> role.equals(p.get("role")))
                .collect(Collectors.toList());
        }
        if (minSelfSufficiency != null) {
            profiles = profiles.stream()
                .filter(p -> ((Number) p.get("selfSufficiency")).doubleValue() >= minSelfSufficiency)
                .collect(Collectors.toList());
        }
        if (maxSelfSufficiency != null) {
            profiles = profiles.stream()
                .filter(p -> ((Number) p.get("selfSufficiency")).doubleValue() <= maxSelfSufficiency)
                .collect(Collectors.toList());
        }

        // Apply sorting
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
            if (comparator != null) {
                profiles = profiles.stream().sorted(comparator).collect(Collectors.toList());
            }
        }

        // Compute summary BEFORE pagination (summary reflects filtered totals)
        int totalElements = profiles.size();
        int totalPages    = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        long exporters      = profiles.stream().filter(p -> "EXPORTADOR_NETO".equals(p.get("classification"))).count();
        long selfSufficient = profiles.stream().filter(p -> "AUTOSUFICIENTE".equals(p.get("classification"))).count();
        long dependent      = profiles.stream().filter(p -> "DEPENDIENTE".equals(p.get("classification"))).count();

        // Apply pagination
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex   = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> paginatedProfiles = profiles.subList(fromIndex, toIndex);

        return ResponseEntity.ok(Map.of(
            "content",       paginatedProfiles,
            "totalElements", totalElements,
            "totalPages",    totalPages,
            "number",        page,
            "size",          size,
            "last",          page >= totalPages - 1,
            "summary", Map.of(
                "totalUsers",     totalElements,
                "exporters",      exporters,
                "selfSufficient", selfSufficient,
                "dependent",      dependent
            )
        ));
    }

    @GetMapping("/user-energy-profile/{userId}")
    public ResponseEntity<?> getUserEnergyProfileById(@PathVariable Long userId) {
        List<Map<String, Object>> profiles = computeUserEnergyProfiles();
        List<Map<String, Object>> filtered = profiles.stream()
            .filter(p -> userId.equals(((Number) p.get("userId")).longValue()))
            .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            return ResponseEntity.ok(filtered.get(0));
        }
        return ResponseEntity.notFound().build();
    }

    private List<Map<String, Object>> computeUserEnergyProfiles() {
        Map<Long, Double> generationMap = new HashMap<>();
        Map<Long, Double> iotConsumptionMap = new HashMap<>();
        Map<Long, Double> boughtMap = new HashMap<>();
        Map<Long, Double> soldMap   = new HashMap<>();

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
            if (generated > iotConsumed + sold * 0.5) {
                classification = "EXPORTADOR_NETO";
            } else if (selfSufficiency >= 60) {
                classification = "AUTOSUFICIENTE";
            } else {
                classification = "DEPENDIENTE";
            }

            Map<String, Object> profileMap = new LinkedHashMap<>();
            profileMap.put("userId",           uid);
            profileMap.put("userName",         user.getNombre());
            profileMap.put("role",             user.getRol());
            profileMap.put("generated",        round(generated));
            profileMap.put("iotConsumed",      round(iotConsumed));
            profileMap.put("bought",           round(bought));
            profileMap.put("sold",             round(sold));
            profileMap.put("netBalance",       round(netBalance));
            profileMap.put("selfSufficiency",  Math.round(selfSufficiency * 10.0) / 10.0);
            profileMap.put("exportPotential",  round(exportPotential));
            profileMap.put("classification",   classification);
            return profileMap;
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

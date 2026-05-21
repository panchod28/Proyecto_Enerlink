package com.enerlink.enerlink.energia.infraestructura.controlador;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.energia.aplicacion.servicio.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/volume")
    public ResponseEntity<?> getEnergyVolume(
            @RequestParam(defaultValue = "month") String groupBy,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "10")    int size) {
        return ResponseEntity.ok(reportService.getEnergyVolume(groupBy, page, size));
    }

    @GetMapping("/commissions")
    public ResponseEntity<?> getCommissions(
            @RequestParam(defaultValue = "month") String groupBy,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "10")    int size) {
        return ResponseEntity.ok(reportService.getCommissions(groupBy, page, size));
    }

    @GetMapping("/market-summary")
    public ResponseEntity<?> getMarketSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String saleType,
            @RequestParam(required = false) Double minAvgPrice) {
        return ResponseEntity.ok(
            reportService.getMarketSummary(startDate, endDate, saleType, minAvgPrice));
    }

    @GetMapping("/producer-efficiency")
    public ResponseEntity<?> getProducerEfficiency(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getProducerEfficiency(page, size));
    }

    @GetMapping("/buyer-activity")
    public ResponseEntity<?> getBuyerActivity(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getBuyerActivity(page, size));
    }

    @GetMapping("/user-ranking")
    public ResponseEntity<?> getUserRanking(
            @RequestParam(defaultValue = "SPEND") String strategy,
            @RequestParam(required = false)        String role,
            @RequestParam(defaultValue = "0")      int page,
            @RequestParam(defaultValue = "20")     int size) {
        return ResponseEntity.ok(
            reportService.getUserRanking(strategy, role, page, size));
    }

    @GetMapping("/analytics-dashboard")
    public ResponseEntity<?> getAnalyticsDashboard() {
        return ResponseEntity.ok(reportService.getAnalyticsDashboard());
    }

    @GetMapping("/transaction-timeline")
    public ResponseEntity<?> getTransactionTimeline(
            @RequestParam(required = false)        String saleType,
            @RequestParam(required = false)        String startDate,
            @RequestParam(required = false)        String endDate,
            @RequestParam(defaultValue = "ASC")    String order,
            @RequestParam(defaultValue = "0")      int page,
            @RequestParam(defaultValue = "10")     int size) {
        return ResponseEntity.ok(
            reportService.getTransactionTimeline(
                saleType, startDate, endDate, order, page, size));
    }

    @GetMapping("/offer-monitoring")
    public ResponseEntity<?> getOfferMonitoring(
            @RequestParam(defaultValue = "ACTIVAS") String state,
            @RequestParam(required = false)          String saleType,
            @RequestParam(required = false)          Double minPrice,
            @RequestParam(required = false)          Double maxPrice,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "15")       int size) {
        return ResponseEntity.ok(
            reportService.getOfferMonitoring(state, saleType, minPrice, maxPrice, page, size));
    }

    @GetMapping("/user-energy-profile")
    public ResponseEntity<?> getUserEnergyProfiles(
            @RequestParam(required = false)    String classification,
            @RequestParam(required = false)    String role,
            @RequestParam(required = false)    Double minSelfSufficiency,
            @RequestParam(required = false)    Double maxSelfSufficiency,
            @RequestParam(required = false)    String sortBy,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getUserEnergyProfiles(
            classification, role, minSelfSufficiency,
            maxSelfSufficiency, sortBy, page, size));
    }

    @GetMapping("/user-energy-profile/{userId}")
    public ResponseEntity<?> getUserEnergyProfileById(@PathVariable Long userId) {
        Map<String, Object> profile = reportService.getUserEnergyProfileById(userId);
        return profile != null
            ? ResponseEntity.ok(profile)
            : ResponseEntity.notFound().build();
    }
}
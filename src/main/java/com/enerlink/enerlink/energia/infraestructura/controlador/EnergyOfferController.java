package com.enerlink.enerlink.energia.infraestructura.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.energia.aplicacion.servicio.EnergyTradingFacade;
import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;

@RestController
@RequestMapping("/api/offers")
public class EnergyOfferController {

    private final EnergyTradingFacade facade;

    public EnergyOfferController(EnergyTradingFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<EnergyOffer> create(
            @RequestBody EnergyOfferRequest request) {

        EnergyOffer offer = facade.publishOffer(
                request.getSaleType(),
                request.getProducerId(),
                request.getKwh(),
                request.getPrice());

        return ResponseEntity.ok(offer);
    }

    @GetMapping
    public ResponseEntity<List<EnergyOffer>> getAll() {
        return ResponseEntity.ok(facade.getActiveOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnergyOffer> getById(@PathVariable Long id) {
        EnergyOffer offer = facade.getOfferById(id);
        return ResponseEntity.ok(offer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnergyOffer> update(
            @PathVariable Long id,
            @RequestBody EnergyOfferRequest request) {

        EnergyOffer updatedOffer = facade.publishOffer(
                request.getSaleType(),
                request.getProducerId(),
                request.getKwh(),
                request.getPrice());

        return ResponseEntity.ok(updatedOffer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facade.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}

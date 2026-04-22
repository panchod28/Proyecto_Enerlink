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
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;

@RestController
@RequestMapping("/api/offers")
public class EnergyOfferController {

    private final EnergyTradingFacade facade;
    private final UserRepositoryPort userRepository;

    public EnergyOfferController(EnergyTradingFacade facade, UserRepositoryPort userRepository) {
        this.facade = facade;
        this.userRepository = userRepository;
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

    @PostMapping("/{offerId}/sale")
    public ResponseEntity<?> executeSale(
            @PathVariable Long offerId,
            @RequestBody SaleRequest request) {
        try {
            User buyer = userRepository.buscarPorId(request.getBuyerId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getBuyerId()));

            Transaction transaction = facade.executeDirectSale(offerId, buyer);
            return ResponseEntity.ok(transaction);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{offerId}/auction")
    public ResponseEntity<?> executeAuction(
            @PathVariable Long offerId,
            @RequestBody AuctionRequest request) {
        try {
            User buyer = userRepository.buscarPorId(request.getBuyerId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getBuyerId()));

            Transaction transaction = facade.executeAuction(offerId, buyer, request.getAmount());
            return ResponseEntity.ok(transaction);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
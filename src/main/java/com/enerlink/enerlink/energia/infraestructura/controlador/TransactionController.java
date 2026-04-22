package com.enerlink.enerlink.energia.infraestructura.controlador;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enerlink.enerlink.energia.aplicacion.servicio.EnergyTradingFacade;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.energia.dominio.puerto.TransactionRepositoryPort;

@RestController
@RequestMapping("/api/transacciones")
public class TransactionController {

    private final TransactionRepositoryPort transactionRepository;

    public TransactionController(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAll(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Long buyerId) {

        if (sellerId != null) {
            return ResponseEntity.ok(transactionRepository.findBySellerId(sellerId));
        }
        if (buyerId != null) {
            return ResponseEntity.ok(transactionRepository.findByBuyerId(buyerId));
        }
        return ResponseEntity.ok(transactionRepository.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
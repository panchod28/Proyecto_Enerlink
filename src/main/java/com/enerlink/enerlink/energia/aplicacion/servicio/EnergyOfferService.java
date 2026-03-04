package com.enerlink.enerlink.energia.aplicacion.servicio;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.enerlink.enerlink.energia.dominio.factory.EnergySaleFactory;
import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.energia.dominio.puerto.EnergyOfferRepositoryPort;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;

@Service
public class EnergyOfferService {

    private final EnergyOfferRepositoryPort repository;
    private final UserRepositoryPort userRepository;
    private final Map<SaleType, EnergySaleFactory> factoryMap;

    public EnergyOfferService(
            EnergyOfferRepositoryPort repository,
            UserRepositoryPort userRepository,
            List<EnergySaleFactory> factories) {

        this.repository = repository;
        this.userRepository = userRepository;

        this.factoryMap = factories.stream()
                .collect(Collectors.toMap(
                        EnergySaleFactory::supports,
                        Function.identity()));
    }

    public EnergyOffer createOffer(
            SaleType saleType,
            Long producerId,
            double kwh,
            double price) {

        User producer = userRepository.buscarPorId(producerId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EnergySaleFactory factory = factoryMap.get(saleType);

        EnergyOffer offer = factory.createEnergyOffer(null, producer, kwh, price);

        SaleProcess process = factory.createSaleProcess();
        process.execute(offer);

        return repository.save(offer);
    }

    public List<EnergyOffer> getAll() {
        return repository.findAll();
    }

    public EnergyOffer getOfferById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + id));
    }

    public EnergyOffer updateOffer(Long id, SaleType saleType, double kwh, double price) {
        EnergyOffer existingOffer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + id));

        // Actualizamos los valores directamente
        existingOffer.setKwh(kwh);
        existingOffer.setPrice(price);
        existingOffer.setSaleType(saleType);

        // Ejecutar proceso si es necesario
        EnergySaleFactory factory = factoryMap.get(saleType);
        SaleProcess process = factory.createSaleProcess();
        process.execute(existingOffer);

        // Guardar el objeto existente
        return repository.save(existingOffer);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

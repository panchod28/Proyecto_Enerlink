package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.puerto.EnergyOfferRepositoryPort;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;

@Component
public class EnergyOfferRepositoryAdapter
                implements EnergyOfferRepositoryPort {

        private final EnergyOfferJpaRepository jpaRepository;
        private final UserRepositoryPort userRepository;

        public EnergyOfferRepositoryAdapter(
                        EnergyOfferJpaRepository jpaRepository,
                        UserRepositoryPort userRepository) {

                this.jpaRepository = jpaRepository;
                this.userRepository = userRepository;
        }

        @Override
        public EnergyOffer save(EnergyOffer offer) {

                EnergyOfferEntity entity = new EnergyOfferEntity();
                entity.setId(offer.getId());
                entity.setProducerId(offer.getProducer().getId());
                entity.setKwh(offer.getKwh());
                entity.setPrice(offer.getPrice());
                entity.setSaleType(offer.getSaleType());

                EnergyOfferEntity saved = jpaRepository.save(entity);

                // 🔥 reconstruimos el User
                User producer = userRepository.buscarPorId(saved.getProducerId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return new EnergyOffer(
                                saved.getId(),
                                producer,
                                saved.getKwh(),
                                saved.getPrice(),
                                saved.getSaleType());
        }

        @Override
        public List<EnergyOffer> findAll() {

                return jpaRepository.findAll()
                                .stream()
                                .map(entity -> {

                                        User producer = userRepository
                                                        .buscarPorId(entity.getProducerId())
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "Usuario no encontrado"));

                                        return new EnergyOffer(
                                                        entity.getId(),
                                                        producer,
                                                        entity.getKwh(),
                                                        entity.getPrice(),
                                                        entity.getSaleType());
                                })
                                .toList();
        }

        @Override
        public Optional<EnergyOffer> findById(Long id) {

                return jpaRepository.findById(id)
                                .map(entity -> {

                                        User producer = userRepository
                                                        .buscarPorId(entity.getProducerId())
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "Usuario no encontrado"));

                                        return new EnergyOffer(
                                                        entity.getId(),
                                                        producer,
                                                        entity.getKwh(),
                                                        entity.getPrice(),
                                                        entity.getSaleType());
                                });
        }

        @Override
        public void deleteById(Long id) {
                jpaRepository.deleteById(id);
        }
}

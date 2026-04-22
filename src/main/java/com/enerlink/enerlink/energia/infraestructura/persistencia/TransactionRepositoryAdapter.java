package com.enerlink.enerlink.energia.infraestructura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.energia.dominio.puerto.TransactionRepositoryPort;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;
import com.enerlink.enerlink.usuario.infraestructura.persistencia.UserEntity;
import com.enerlink.enerlink.usuario.infraestructura.persistencia.UserJpaRepository;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

        private final TransactionJpaRepository jpaRepository;
        private final EnergyOfferJpaRepository offerJpaRepository;
        private final UserJpaRepository userJpaRepository;
        private final UserRepositoryPort userRepository;

        public TransactionRepositoryAdapter(
                        TransactionJpaRepository jpaRepository,
                        EnergyOfferJpaRepository offerJpaRepository,
                        UserJpaRepository userJpaRepository,
                        UserRepositoryPort userRepository) {
                this.jpaRepository = jpaRepository;
                this.offerJpaRepository = offerJpaRepository;
                this.userJpaRepository = userJpaRepository;
                this.userRepository = userRepository;
        }

        @Override
        public Transaction save(Transaction transaction) {
                TransactionEntity entity = new TransactionEntity();
                entity.setId(transaction.getId());

                EnergyOfferEntity offerEntity = offerJpaRepository.findById(transaction.getOffer().getId())
                                .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));
                entity.setOffer(offerEntity);

                UserEntity buyerEntity = userJpaRepository.findById(transaction.getBuyer().getId())
                                .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));
                entity.setBuyer(buyerEntity);

                UserEntity sellerEntity = userJpaRepository.findById(transaction.getSeller().getId())
                                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
                entity.setSeller(sellerEntity);

                entity.setKwh(transaction.getKwh());
                entity.setPrice(transaction.getPrice());
                entity.setTimestamp(transaction.getTimestamp());

                TransactionEntity saved = jpaRepository.save(entity);

                EnergyOffer savedOffer = offerJpaRepository.findById(saved.getOffer().getId())
                                .map(e -> {
                                        User producer = userRepository.buscarPorId(e.getProducerId())
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "Usuario no encontrado"));
                                        return new EnergyOffer(
                                                        e.getId(),
                                                        producer,
                                                        e.getKwh(),
                                                        e.getPrice(),
                                                        e.getSaleType());
                                })
                                .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

                User savedBuyer = userRepository.buscarPorId(buyerEntity.getId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                User savedSeller = userRepository.buscarPorId(sellerEntity.getId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return Transaction.builder()
                                .id(saved.getId())
                                .offer(savedOffer)
                                .buyer(savedBuyer)
                                .seller(savedSeller)
                                .kwh(saved.getKwh())
                                .price(saved.getPrice())
                                .timestamp(saved.getTimestamp())
                                .build();
        }

        @Override
        public List<Transaction> findAll() {
                return jpaRepository.findAll()
                                .stream()
                                .map(entity -> mapToDomain(entity))
                                .toList();
        }

        @Override
        public Optional<Transaction> findById(Long id) {
                return jpaRepository.findById(id)
                                .map(entity -> mapToDomain(entity));
        }

        private Transaction mapToDomain(TransactionEntity entity) {
                EnergyOffer offer = offerJpaRepository.findById(entity.getOffer().getId())
                                .map(e -> {
                                        User producer = userRepository.buscarPorId(e.getProducerId())
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        "Usuario no encontrado"));
                                        return new EnergyOffer(
                                                        e.getId(),
                                                        producer,
                                                        e.getKwh(),
                                                        e.getPrice(),
                                                        e.getSaleType());
                                })
                                .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

                User buyer = userRepository.buscarPorId(entity.getBuyer().getId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                User seller = userRepository.buscarPorId(entity.getSeller().getId())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return Transaction.builder()
                                .id(entity.getId())
                                .offer(offer)
                                .buyer(buyer)
                                .seller(seller)
                                .kwh(entity.getKwh())
                                .price(entity.getPrice())
                                .timestamp(entity.getTimestamp())
                                .build();
        }

        @Override
        public List<Transaction> findBySellerId(Long sellerId) {
                return jpaRepository.findBySellerId(sellerId)
                                .stream()
                                .map(entity -> mapToDomain(entity))
                                .toList();
        }

        @Override
        public List<Transaction> findByBuyerId(Long buyerId) {
                return jpaRepository.findByBuyerId(buyerId)
                                .stream()
                                .map(entity -> mapToDomain(entity))
                                .toList();
        }
}
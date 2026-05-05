package com.enerlink.enerlink.energia.aplicacion.servicio;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.enerlink.enerlink.energia.dominio.componente.ConcreteTransactionComponent;
import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;
import com.enerlink.enerlink.energia.dominio.decorador.AuditingTransactionDecorator;
import com.enerlink.enerlink.energia.dominio.decorador.FeeTransactionDecorator;
import com.enerlink.enerlink.energia.dominio.decorador.ValidatingTransactionDecorator;
import com.enerlink.enerlink.energia.dominio.factory.AuctionSaleFactory;
import com.enerlink.enerlink.energia.dominio.factory.DirectSaleFactory;
import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.energia.dominio.puerto.EnergyOfferRepositoryPort;
import com.enerlink.enerlink.energia.dominio.puerto.TransactionRepositoryPort;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.util.List;

@Service
public class EnergyTradingFacade {

    private final EnergyOfferService energyOfferService;
    private final DirectSaleFactory directSaleFactory;
    private final AuctionSaleFactory auctionSaleFactory;
    private final EnergyOfferRepositoryPort repositoryPort;
    private final TransactionRepositoryPort transactionRepositoryPort;

    public EnergyTradingFacade(
            EnergyOfferService energyOfferService,
            DirectSaleFactory directSaleFactory,
            AuctionSaleFactory auctionSaleFactory,
            EnergyOfferRepositoryPort repositoryPort,
            TransactionRepositoryPort transactionRepositoryPort) {
        this.energyOfferService = energyOfferService;
        this.directSaleFactory = directSaleFactory;
        this.auctionSaleFactory = auctionSaleFactory;
        this.repositoryPort = repositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    public EnergyOffer publishOffer(SaleType saleType, Long producerId, double kwh, double price) {
        return energyOfferService.createOffer(saleType, producerId, kwh, price);
    }

    public Transaction executeDirectSale(Long offerId, User buyer) {
        EnergyOffer offer = repositoryPort.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + offerId));

        if (offer.getSaleType() != SaleType.DIRECT) {
            throw new IllegalStateException(
                    "La oferta no es de tipo venta directa. Tipo actual: " + offer.getSaleType());
        }

        SaleProcess saleProcess = directSaleFactory.createSaleProcess();
        Transaction transaction = saleProcess.execute(offer, buyer, offer.getKwh());
        Transaction decoratedTransaction = applyDecoratorChain(transaction);
        Transaction savedTransaction = transactionRepositoryPort.save(decoratedTransaction);

        offer.setAvailable(false);
        repositoryPort.save(offer);

        return savedTransaction;
    }

    public Transaction executeAuction(Long offerId, User buyer, double bidAmount) {
        EnergyOffer offer = repositoryPort.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + offerId));

        if (offer.getSaleType() != SaleType.AUCTION) {
            throw new IllegalStateException("La oferta no es de tipo subasta. Tipo actual: " + offer.getSaleType());
        }

        if (offer.getPrice() > 0 && bidAmount < offer.getPrice()) {
            throw new IllegalArgumentException("La oferta debe ser mayor o igual al precio base: " + offer.getPrice());
        }

        SaleProcess auctionProcess = auctionSaleFactory.createSaleProcess();
        Transaction transaction = auctionProcess.execute(offer, buyer, offer.getKwh(), bidAmount);
        Transaction decoratedTransaction = applyDecoratorChain(transaction);
        return transactionRepositoryPort.save(decoratedTransaction);
    }

    public List<EnergyOffer> getActiveOffers() {
        return energyOfferService.getAll();
    }

    public Page<EnergyOffer> getActiveOffers(Pageable pageable) {
        return energyOfferService.getAll(pageable);
    }

    public Page<EnergyOffer> getOffersWithFilters(
            String saleType,
            Double minPrice,
            Double maxPrice,
            Double minKwh,
            Double maxKwh,
            int page,
            int size,
            String sortBy) {

        SaleType type = (saleType != null && !saleType.isBlank())
            ? SaleType.valueOf(saleType) : null;

        Sort sort = switch (sortBy != null ? sortBy : "") {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "kwh_asc"    -> Sort.by(Sort.Direction.ASC, "kwh");
            case "kwh_desc"   -> Sort.by(Sort.Direction.DESC, "kwh");
            default           -> Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return energyOfferService.getWithFilters(type, minPrice, maxPrice, minKwh, maxKwh, pageable);
    }

    public List<EnergyOffer> getOffersByProducer(Long producerId) {
        return energyOfferService.getByProducerId(producerId);
    }

    public EnergyOffer getOfferById(Long id) {
        return repositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + id));
    }

    public void deleteOffer(Long id) {
        repositoryPort.deleteById(id);
    }

    private Transaction applyDecoratorChain(Transaction transaction) {
        TransactionComponent component = new ConcreteTransactionComponent(transaction);
        component = new ValidatingTransactionDecorator(component);
        component = new FeeTransactionDecorator(component, 0.0);
        component = new AuditingTransactionDecorator(component);

        return Transaction.builder()
                .id(component.getId())
                .offer(component.getOffer())
                .buyer(component.getBuyer())
                .seller(component.getSeller())
                .kwh(component.getKwh())
                .price(component.getPrice())
                .timestamp(component.getTimestamp())
                .build();
    }

    public long countByType(SaleType saleType) {
        return energyOfferService.countAvailableByType(saleType);
    }
}
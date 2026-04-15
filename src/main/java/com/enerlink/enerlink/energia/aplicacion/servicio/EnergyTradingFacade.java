package com.enerlink.enerlink.energia.aplicacion.servicio;

import org.springframework.stereotype.Service;

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
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.util.List;

/**
 * Facade that provides a unified interface to the energy trading subsystem.
 * 
 * This class hides the complexity of the following subsystems:
 * <ul>
 *   <li>{@link EnergyOfferService} - Manages energy offer lifecycle (create, find, update state)</li>
 *   <li>{@link DirectSaleFactory} - Creates direct sale processes for fixed-price transactions</li>
 *   <li>{@link AuctionSaleFactory} - Creates auction sale processes for bid-based transactions</li>
 *   <li>{@link SaleProcess} implementations - Execute sale transactions (direct and auction)</li>
 *   <li>Transaction Decorator Chain - Applies validation, fees, and auditing to transactions</li>
 * </ul>
 * 
 * Clients can perform energy trading operations without knowing the internal
 * details of offer management, sale process selection, or transaction decoration.
 */
@Service
public class EnergyTradingFacade {

    private final EnergyOfferService energyOfferService;
    private final DirectSaleFactory directSaleFactory;
    private final AuctionSaleFactory auctionSaleFactory;
    private final EnergyOfferRepositoryPort repositoryPort;

    public EnergyTradingFacade(
            EnergyOfferService energyOfferService,
            DirectSaleFactory directSaleFactory,
            AuctionSaleFactory auctionSaleFactory,
            EnergyOfferRepositoryPort repositoryPort) {
        this.energyOfferService = energyOfferService;
        this.directSaleFactory = directSaleFactory;
        this.auctionSaleFactory = auctionSaleFactory;
        this.repositoryPort = repositoryPort;
    }

    public EnergyOffer publishOffer(SaleType saleType, Long producerId, double kwh, double price) {
        return energyOfferService.createOffer(saleType, producerId, kwh, price);
    }

    public Transaction executeDirectSale(Long offerId, User buyer) {
        EnergyOffer offer = repositoryPort.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + offerId));

        if (offer.getSaleType() != SaleType.DIRECT) {
            throw new IllegalStateException("La oferta no es de tipo venta directa. Tipo actual: " + offer.getSaleType());
        }

        SaleProcess saleProcess = directSaleFactory.createSaleProcess();
        Transaction transaction = saleProcess.execute(offer, buyer, offer.getKwh());

        return applyDecoratorChain(transaction);
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

        return applyDecoratorChain(transaction);
    }

    public List<EnergyOffer> getActiveOffers() {
        return energyOfferService.getAll();
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
}
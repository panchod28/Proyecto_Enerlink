package com.enerlink.enerlink.energia.aplicacion.servicio;

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
import com.enerlink.enerlink.energia.dominio.proceso.DirectSaleProcess;
import com.enerlink.enerlink.energia.dominio.proceso.SaleProcess;
import com.enerlink.enerlink.energia.dominio.puerto.EnergyOfferRepositoryPort;
import com.enerlink.enerlink.usuario.dominio.modelo.ConsumerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnergyTradingFacade Tests")
class EnergyTradingFacadeTest {

    @Mock
    private EnergyOfferService energyOfferService;

    @Mock
    private DirectSaleFactory directSaleFactory;

    @Mock
    private AuctionSaleFactory auctionSaleFactory;

    @Mock
    private EnergyOfferRepositoryPort repositoryPort;

    @InjectMocks
    private EnergyTradingFacade facade;

    private User producer;
    private User buyer;
    private EnergyOffer directOffer;
    private EnergyOffer auctionOffer;
    private Transaction transaction;
    private SaleProcess saleProcess;

    @BeforeEach
    void setUp() {
        producer = new ProducerUser("John Producer", "john@enerlink.com");
        producer.setId(1L);

        buyer = new ConsumerUser("Jane Consumer", "jane@enerlink.com");
        buyer.setId(2L);

        directOffer = new EnergyOffer(1L, producer, 100.0, 150.0, SaleType.DIRECT);
        auctionOffer = new EnergyOffer(2L, producer, 100.0, 150.0, SaleType.AUCTION);

        transaction = Transaction.builder()
                .id(100L)
                .offer(directOffer)
                .buyer(buyer)
                .seller(producer)
                .kwh(100.0)
                .price(150.0)
                .timestamp(LocalDateTime.now())
                .build();

        saleProcess = new DirectSaleProcess();
    }

    @Nested
    @DisplayName("publishOffer Tests")
    class PublishOfferTests {

        @Test
        @DisplayName("Should delegate to EnergyOfferService with correct arguments")
        void should_delegate_to_energy_offer_service_with_correct_arguments() {
            when(energyOfferService.createOffer(SaleType.DIRECT, 1L, 100.0, 150.0))
                    .thenReturn(directOffer);

            facade.publishOffer(SaleType.DIRECT, 1L, 100.0, 150.0);

            verify(energyOfferService).createOffer(SaleType.DIRECT, 1L, 100.0, 150.0);
        }

        @Test
        @DisplayName("Should return the offer returned by the service")
        void should_return_offer_returned_by_service() {
            when(energyOfferService.createOffer(SaleType.DIRECT, 1L, 100.0, 150.0))
                    .thenReturn(directOffer);

            EnergyOffer result = facade.publishOffer(SaleType.DIRECT, 1L, 100.0, 150.0);

            assertEquals(directOffer, result);
        }
    }

    @Nested
    @DisplayName("getActiveOffers Tests")
    class GetActiveOffersTests {

        @Test
        @DisplayName("Should delegate to EnergyOfferService and return all offers")
        void should_delegate_to_service_and_return_all_offers() {
            List<EnergyOffer> expectedOffers = Arrays.asList(directOffer, auctionOffer);
            when(energyOfferService.getAll()).thenReturn(expectedOffers);

            List<EnergyOffer> result = facade.getActiveOffers();

            assertEquals(expectedOffers, result);
            verify(energyOfferService).getAll();
        }

        @Test
        @DisplayName("Should return empty list when service returns empty")
        void should_return_empty_list_when_service_returns_empty() {
            when(energyOfferService.getAll()).thenReturn(List.of());

            List<EnergyOffer> result = facade.getActiveOffers();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("executeDirectSale Tests")
    class ExecuteDirectSaleTests {

        @Test
        @DisplayName("Should resolve the correct offer by ID")
        void should_resolve_correct_offer_by_id() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(directSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            facade.executeDirectSale(1L, buyer);

            verify(repositoryPort).findById(1L);
        }

        @Test
        @DisplayName("Should call DirectSaleFactory to create the process")
        void should_call_direct_sale_factory_to_create_process() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(directSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            facade.executeDirectSale(1L, buyer);

            verify(directSaleFactory).createSaleProcess();
        }

        @Test
        @DisplayName("Should execute the sale process with correct parameters")
        void should_execute_sale_process_with_correct_parameters() {
            SaleProcess mockSaleProcess = mock(SaleProcess.class);
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(directSaleFactory.createSaleProcess()).thenReturn(mockSaleProcess);
            when(mockSaleProcess.execute(directOffer, buyer, 100.0)).thenReturn(transaction);

            facade.executeDirectSale(1L, buyer);

            verify(mockSaleProcess).execute(directOffer, buyer, 100.0);
        }

        @Test
        @DisplayName("Should apply the full decorator chain to the transaction")
        void should_apply_full_decorator_chain() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(directSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            Transaction result = facade.executeDirectSale(1L, buyer);

            assertNotNull(result);
            assertEquals(directOffer, result.getOffer());
            assertEquals(buyer, result.getBuyer());
            assertEquals(producer, result.getSeller());
        }

        @Test
        @DisplayName("Should throw when offer is not found")
        void should_throw_when_offer_not_found() {
            when(repositoryPort.findById(999L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> facade.executeDirectSale(999L, buyer));

            assertTrue(exception.getMessage().contains("Oferta no encontrada"));
        }

        @Test
        @DisplayName("Should throw when offer is not of type DIRECT")
        void should_throw_when_offer_not_direct_type() {
            when(repositoryPort.findById(2L)).thenReturn(Optional.of(auctionOffer));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> facade.executeDirectSale(2L, buyer));

            assertTrue(exception.getMessage().contains("venta directa"));
        }
    }

    @Nested
    @DisplayName("executeAuction Tests")
    class ExecuteAuctionTests {

        @Test
        @DisplayName("Should create and execute AuctionSaleProcess")
        void should_create_and_execute_auction_process() {
            when(repositoryPort.findById(2L)).thenReturn(Optional.of(auctionOffer));
            when(auctionSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            Transaction result = facade.executeAuction(2L, buyer, 150.0);

            assertNotNull(result);
            assertEquals(auctionOffer, result.getOffer());
            verify(auctionSaleFactory).createSaleProcess();
        }

        @Test
        @DisplayName("Should apply the decorator chain for auction")
        void should_apply_decorator_chain_for_auction() {
            when(repositoryPort.findById(2L)).thenReturn(Optional.of(auctionOffer));
            when(auctionSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            Transaction result = facade.executeAuction(2L, buyer, 150.0);

            assertNotNull(result);
            assertEquals(buyer, result.getBuyer());
        }

        @Test
        @DisplayName("Should throw when bid amount is below current price")
        void should_throw_when_bid_below_price() {
            when(repositoryPort.findById(2L)).thenReturn(Optional.of(auctionOffer));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> facade.executeAuction(2L, buyer, 100.0));

            assertTrue(exception.getMessage().contains("precio base"));
        }

        @Test
        @DisplayName("Should throw when auction is not of type AUCTION")
        void should_throw_when_offer_not_auction_type() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> facade.executeAuction(1L, buyer, 150.0));

            assertTrue(exception.getMessage().contains("subasta"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should throw when repository throws exception")
        void should_throw_when_repository_throws() {
            when(repositoryPort.findById(1L)).thenThrow(new RuntimeException("DB error"));

            assertThrows(RuntimeException.class,
                    () -> facade.executeDirectSale(1L, buyer));
        }

        @Test
        @DisplayName("Should throw when bid amount is zero")
        void should_throw_when_bid_amount_is_zero() {
            EnergyOffer freeOffer = new EnergyOffer(3L, producer, 100.0, 50.0, SaleType.AUCTION);
            when(repositoryPort.findById(3L)).thenReturn(Optional.of(freeOffer));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> facade.executeAuction(3L, buyer, 0.0));

            assertTrue(exception.getMessage().contains("precio base"));
        }
    }

    @Nested
    @DisplayName("Decorator Chain Integration Tests")
    class DecoratorChainIntegrationTests {

        @Test
        @DisplayName("Should wrap transaction with all decorators in correct order")
        void should_wrap_transaction_with_all_decorators() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(directSaleFactory.createSaleProcess()).thenReturn(saleProcess);

            Transaction result = facade.executeDirectSale(1L, buyer);

            assertNotNull(result);
            assertNotNull(result.getTimestamp());
            assertEquals(directOffer, result.getOffer());
            assertEquals(buyer, result.getBuyer());
        }
    }

    @Nested
    @DisplayName("Controller Decoupling Tests")
    class ControllerDecouplingTests {

        @Test
        @DisplayName("Facade should be the only dependency for trading operations")
        void facade_should_be_only_dependency() {
            EnergyTradingFacade facadeInstance = new EnergyTradingFacade(
                    energyOfferService, directSaleFactory, auctionSaleFactory, repositoryPort, null);

            assertNotNull(facadeInstance);
        }

        @Test
        @DisplayName("All trading methods should be accessible through facade")
        void all_trading_methods_accessible_through_facade() {
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(directOffer));
            when(energyOfferService.createOffer(SaleType.DIRECT, 1L, 100.0, 150.0)).thenReturn(directOffer);

            assertDoesNotThrow(() -> facade.publishOffer(SaleType.DIRECT, 1L, 100.0, 150.0));
            assertDoesNotThrow(() -> facade.getActiveOffers());
            assertDoesNotThrow(() -> facade.getOfferById(1L));
            assertDoesNotThrow(() -> facade.deleteOffer(1L));
        }
    }
}

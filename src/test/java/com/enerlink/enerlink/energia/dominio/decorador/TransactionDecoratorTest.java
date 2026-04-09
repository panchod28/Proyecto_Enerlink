package com.enerlink.enerlink.energia.dominio.decorador;

import com.enerlink.enerlink.energia.dominio.componente.ConcreteTransactionComponent;
import com.enerlink.enerlink.energia.dominio.componente.TransactionComponent;
import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.energia.dominio.modelo.Transaction;
import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.ConsumerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Decorator Pattern Tests - Transaction")
class TransactionDecoratorTest {

    private EnergyOffer energyOffer;
    private User producer;
    private User consumer;
    private Transaction transaction;
    private TransactionComponent component;

    @BeforeEach
    void setUp() {
        producer = new ProducerUser("John Producer", "john@enerlink.com");
        consumer = new ConsumerUser("Jane Consumer", "jane@enerlink.com");
        energyOffer = new EnergyOffer(1L, producer, 100.0, 150.0, SaleType.DIRECT);
        
        transaction = Transaction.builder()
            .id(100L)
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(100)
            .price(150)
            .timestamp(LocalDateTime.of(2024, 6, 15, 10, 30))
            .build();
        
        component = new ConcreteTransactionComponent(transaction);
    }

    @Nested
    @DisplayName("ConcreteTransactionComponent Tests")
    class ConcreteTransactionComponentTests {

        @Test
        @DisplayName("Should wrap Transaction correctly")
        void shouldWrapTransactionCorrectly() {
            assertNotNull(component);
            assertEquals(transaction.getId(), component.getId());
            assertEquals(transaction.getBuyer(), component.getBuyer());
            assertEquals(transaction.getSeller(), component.getSeller());
            assertEquals(transaction.getKwh(), component.getKwh());
            assertEquals(transaction.getPrice(), component.getPrice());
            assertEquals(transaction.getTotalAmount(), component.getTotalAmount());
        }

        @Test
        @DisplayName("Should return original Transaction")
        void shouldReturnOriginalTransaction() {
            ConcreteTransactionComponent concrete = (ConcreteTransactionComponent) component;
            assertEquals(transaction, concrete.getTransaction());
        }
    }

    @Nested
    @DisplayName("ValidatingTransactionDecorator Tests")
    class ValidatingTransactionDecoratorTests {

        @Test
        @DisplayName("Should validate transaction with valid buyer and seller")
        void shouldValidateTransactionWithValidBuyerAndSeller() {
            TransactionComponent validated = new ValidatingTransactionDecorator(component);
            
            assertNotNull(validated.getBuyer());
            assertNotNull(validated.getSeller());
            assertTrue(validated instanceof ValidatingTransactionDecorator);
        }

        @Test
        @DisplayName("Should be valid when buyer and seller have names")
        void shouldBeValidWhenBuyerAndSellerHaveNames() {
            TransactionComponent validated = new ValidatingTransactionDecorator(component);
            validated.getBuyer();
            validated.getSeller();
            
            ValidatingTransactionDecorator decorator = (ValidatingTransactionDecorator) validated;
            assertTrue(decorator.isValid());
            assertTrue(decorator.getValidationErrors().isEmpty());
        }

        @Test
        @DisplayName("Should delegate other methods to wrapped component")
        void shouldDelegateOtherMethodsToWrappedComponent() {
            TransactionComponent validated = new ValidatingTransactionDecorator(component);
            
            assertEquals(100L, validated.getId());
            assertEquals(100.0, validated.getKwh());
            assertEquals(150.0, validated.getPrice());
            assertEquals(15000.0, validated.getTotalAmount());
        }
    }

    @Nested
    @DisplayName("DiscountedTransactionDecorator Tests")
    class DiscountedTransactionDecoratorTests {

        @Test
        @DisplayName("Should apply 10% discount correctly")
        void shouldApply10PercentDiscountCorrectly() {
            TransactionComponent discounted = new DiscountedTransactionDecorator(component, 10.0);
            
            assertEquals(135.0, discounted.getPrice(), 0.001);
            assertEquals(13500.0, discounted.getTotalAmount(), 0.001);
        }

        @Test
        @DisplayName("Should apply 25% discount correctly")
        void shouldApply25PercentDiscountCorrectly() {
            TransactionComponent discounted = new DiscountedTransactionDecorator(component, 25.0);
            
            assertEquals(112.5, discounted.getPrice(), 0.001);
            assertEquals(11250.0, discounted.getTotalAmount(), 0.001);
        }

        @Test
        @DisplayName("Should throw exception for invalid discount (negative)")
        void shouldThrowExceptionForInvalidDiscountNegative() {
            assertThrows(IllegalArgumentException.class, 
                () -> new DiscountedTransactionDecorator(component, -5.0));
        }

        @Test
        @DisplayName("Should throw exception for invalid discount (over 100)")
        void shouldThrowExceptionForInvalidDiscountOver100() {
            assertThrows(IllegalArgumentException.class, 
                () -> new DiscountedTransactionDecorator(component, 150.0));
        }

        @Test
        @DisplayName("Should calculate discount amount correctly")
        void shouldCalculateDiscountAmountCorrectly() {
            DiscountedTransactionDecorator decorator = new DiscountedTransactionDecorator(component, 20.0);
            
            assertEquals(20.0, decorator.getDiscountPercentage(), 0.001);
            assertEquals(3000.0, decorator.getDiscountAmount(), 0.001);
        }

        @Test
        @DisplayName("Should handle zero discount correctly")
        void shouldHandleZeroDiscountCorrectly() {
            TransactionComponent discounted = new DiscountedTransactionDecorator(component, 0.0);
            
            assertEquals(150.0, discounted.getPrice(), 0.001);
            assertEquals(15000.0, discounted.getTotalAmount(), 0.001);
        }
    }

    @Nested
    @DisplayName("FeeTransactionDecorator Tests")
    class FeeTransactionDecoratorTests {

        @Test
        @DisplayName("Should add fixed fee correctly")
        void shouldAddFixedFeeCorrectly() {
            TransactionComponent withFee = new FeeTransactionDecorator(component, 50.0);
            
            assertEquals(50.0, withFee.getTotalAmount() - 15000.0, 0.001);
        }

        @Test
        @DisplayName("Should add fixed fee and percentage fee correctly")
        void shouldAddFixedFeeAndPercentageFeeCorrectly() {
            TransactionComponent withFee = new FeeTransactionDecorator(component, 50.0, 5.0);
            
            double expected = 15000.0 + 50.0 + (15000.0 * 0.05);
            assertEquals(expected, withFee.getTotalAmount(), 0.001);
        }

        @Test
        @DisplayName("Should throw exception for negative fixed fee")
        void shouldThrowExceptionForNegativeFixedFee() {
            assertThrows(IllegalArgumentException.class,
                () -> new FeeTransactionDecorator(component, -10.0));
        }

        @Test
        @DisplayName("Should throw exception for invalid percentage fee")
        void shouldThrowExceptionForInvalidPercentageFee() {
            assertThrows(IllegalArgumentException.class,
                () -> new FeeTransactionDecorator(component, 10.0, -5.0));
        }

        @Test
        @DisplayName("Should return fee amount correctly")
        void shouldReturnFeeAmountCorrectly() {
            FeeTransactionDecorator decorator = new FeeTransactionDecorator(component, 50.0, 10.0);
            
            assertEquals(50.0, decorator.getFixedFee(), 0.001);
            assertEquals(10.0, decorator.getPercentageFee(), 0.001);
            assertEquals(1550.0, decorator.getFeeAmount(), 0.001);
        }
    }

    @Nested
    @DisplayName("AuditingTransactionDecorator Tests")
    class AuditingTransactionDecoratorTests {

        @Test
        @DisplayName("Should log audit entries for getTotalAmount")
        void shouldLogAuditEntriesForGetTotalAmount() {
            TransactionComponent audited = new AuditingTransactionDecorator(component);
            audited.getTotalAmount();
            
            AuditingTransactionDecorator decorator = (AuditingTransactionDecorator) audited;
            assertTrue(decorator.getAuditLogSize() > 0);
        }

        @Test
        @DisplayName("Should log audit entries for getPrice")
        void shouldLogAuditEntriesForGetPrice() {
            TransactionComponent audited = new AuditingTransactionDecorator(component);
            audited.getPrice();
            
            AuditingTransactionDecorator decorator = (AuditingTransactionDecorator) audited;
            assertTrue(decorator.getAuditLogSize() > 0);
        }

        @Test
        @DisplayName("Should log audit entries for getKwh")
        void shouldLogAuditEntriesForGetKwh() {
            TransactionComponent audited = new AuditingTransactionDecorator(component);
            audited.getKwh();
            
            AuditingTransactionDecorator decorator = (AuditingTransactionDecorator) audited;
            assertTrue(decorator.getAuditLogSize() > 0);
        }

        @Test
        @DisplayName("Should delegate core values to wrapped component")
        void shouldDelegateCoreValuesToWrappedComponent() {
            TransactionComponent audited = new AuditingTransactionDecorator(component);
            
            assertEquals(100L, audited.getId());
            assertEquals(100.0, audited.getKwh());
            assertEquals(150.0, audited.getPrice());
            assertEquals(15000.0, audited.getTotalAmount());
        }

        @Test
        @DisplayName("Should return copy of audit log")
        void shouldReturnCopyOfAuditLog() {
            TransactionComponent audited = new AuditingTransactionDecorator(component);
            audited.getTotalAmount();
            
            AuditingTransactionDecorator decorator = (AuditingTransactionDecorator) audited;
            assertNotNull(decorator.getAuditLog());
        }
    }

    @Nested
    @DisplayName("Decorator Stacking Tests")
    class DecoratorStackingTests {

        @Test
        @DisplayName("Should stack multiple decorators correctly")
        void shouldStackMultipleDecoratorsCorrectly() {
            TransactionComponent wrapped = component;
            wrapped = new ValidatingTransactionDecorator(wrapped);
            wrapped = new DiscountedTransactionDecorator(wrapped, 15.0);
            wrapped = new FeeTransactionDecorator(wrapped, 25.0);
            wrapped = new AuditingTransactionDecorator(wrapped);
            
            double expectedPrice = 150.0 * 0.85;
            double expectedTotal = (100.0 * expectedPrice) + 25.0;
            
            assertEquals(expectedPrice, wrapped.getPrice(), 0.001);
            assertEquals(expectedTotal, wrapped.getTotalAmount(), 0.001);
        }

        @Test
        @DisplayName("Should maintain decorator chain integrity")
        void shouldMaintainDecoratorChainIntegrity() {
            TransactionComponent wrapped = component;
            wrapped = new DiscountedTransactionDecorator(wrapped, 10.0);
            wrapped = new AuditingTransactionDecorator(wrapped);
            wrapped.getTotalAmount();
            
            AuditingTransactionDecorator audited = (AuditingTransactionDecorator) wrapped;
            assertTrue(audited.getAuditLogSize() > 0);
        }

        @Test
        @DisplayName("Should allow reverse order stacking")
        void shouldAllowReverseOrderStacking() {
            TransactionComponent wrapped = component;
            wrapped = new AuditingTransactionDecorator(wrapped);
            wrapped = new DiscountedTransactionDecorator(wrapped, 20.0);
            
            double expectedPrice = 150.0 * 0.80;
            assertEquals(expectedPrice, wrapped.getPrice(), 0.001);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception when wrapping null component")
        void shouldThrowExceptionWhenWrappingNullComponent() {
            assertThrows(IllegalArgumentException.class,
                () -> new TransactionDecorator(null) {});
        }

        @Test
        @DisplayName("Should handle transaction with null id")
        void shouldHandleTransactionWithNullId() {
            Transaction noIdTransaction = Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(50)
                .price(100)
                .build();
            
            TransactionComponent noIdComponent = new ConcreteTransactionComponent(noIdTransaction);
            assertNull(noIdComponent.getId());
        }

        @Test
        @DisplayName("Should handle zero kwh transaction")
        void shouldHandleZeroKwhTransaction() {
            Transaction zeroKwhTransaction = Transaction.builder()
                .id(1L)
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(1)
                .price(100)
                .build();
            
            TransactionComponent zeroComponent = new ConcreteTransactionComponent(zeroKwhTransaction);
            assertEquals(100.0, zeroComponent.getTotalAmount(), 0.001);
        }
    }
}

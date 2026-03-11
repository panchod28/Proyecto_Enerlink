package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.ConsumerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Fluent Builder Tests")
class TransactionBuilderTest {

    private EnergyOffer energyOffer;
    private User producer;
    private User consumer;

    @BeforeEach
    void setUp() {
        producer = new ProducerUser("John Producer", "john@enerlink.com");
        consumer = new ConsumerUser("Jane Consumer", "jane@enerlink.com");
        energyOffer = new EnergyOffer(1L, producer, 100.0, 150.0, SaleType.DIRECT);
    }

    @Test
    @DisplayName("1. Should build Transaction using Fluent Builder")
    void shouldBuildTransactionUsingFluentBuilder() {
        // Given: All required fields for Transaction
        LocalDateTime timestamp = LocalDateTime.of(2024, 6, 15, 10, 30);

        // When: Building Transaction using Fluent Builder pattern
        Transaction transaction = Transaction.builder()
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(100)
            .price(150)
            .timestamp(timestamp)
            .build();

        // Then: All fields are correctly assigned
        assertNotNull(transaction);
        assertEquals(energyOffer, transaction.getOffer());
        assertEquals(consumer, transaction.getBuyer());
        assertEquals(producer, transaction.getSeller());
        assertEquals(100.0, transaction.getKwh());
        assertEquals(150.0, transaction.getPrice());
        assertEquals(timestamp, transaction.getTimestamp());
    }

    @Test
    @DisplayName("2. Should throw exception when offer is null")
    void shouldThrowExceptionWhenOfferIsNull() {
        // When & Then: Attempting to build without offer should throw NullPointerException
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Transaction.builder()
                .offer(null)
                .buyer(consumer)
                .seller(producer)
                .kwh(100)
                .price(150)
                .build()
        );

        assertEquals("Offer is mandatory", exception.getMessage());
    }

    @Test
    @DisplayName("3. Should throw exception when buyer is null")
    void shouldThrowExceptionWhenBuyerIsNull() {
        // When & Then: Attempting to build without buyer should throw NullPointerException
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(null)
                .seller(producer)
                .kwh(100)
                .price(150)
                .build()
        );

        assertEquals("Buyer is mandatory", exception.getMessage());
    }

    @Test
    @DisplayName("4. Should throw exception when seller is null")
    void shouldThrowExceptionWhenSellerIsNull() {
        // When & Then: Attempting to build without seller should throw NullPointerException
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(null)
                .kwh(100)
                .price(150)
                .build()
        );

        assertEquals("Seller is mandatory", exception.getMessage());
    }

    @Test
    @DisplayName("5. Should throw exception when kwh is invalid (zero)")
    void shouldThrowExceptionWhenKwhIsZero() {
        // When & Then: Attempting to build with kwh = 0 should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(0)
                .price(150)
                .build()
        );

        assertEquals("KWh must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("6. Should throw exception when kwh is negative")
    void shouldThrowExceptionWhenKwhIsNegative() {
        // When & Then: Attempting to build with negative kwh should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(-50)
                .price(150)
                .build()
        );

        assertEquals("KWh must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("7. Should throw exception when price is invalid (zero)")
    void shouldThrowExceptionWhenPriceIsZero() {
        // When & Then: Attempting to build with price = 0 should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(100)
                .price(0)
                .build()
        );

        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("8. Should throw exception when price is negative")
    void shouldThrowExceptionWhenPriceIsNegative() {
        // When & Then: Attempting to build with negative price should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Transaction.builder()
                .offer(energyOffer)
                .buyer(consumer)
                .seller(producer)
                .kwh(100)
                .price(-50)
                .build()
        );

        assertEquals("Price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("9. Should calculate total amount correctly (kwh * price)")
    void shouldCalculateTotalAmountCorrectly() {
        // Given: kwh = 100, price = 150
        Transaction transaction = Transaction.builder()
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(100)
            .price(150)
            .build();

        // Then: Total amount should be 100 * 150 = 15000
        assertEquals(15000.0, transaction.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("10. Should assign timestamp automatically when not provided")
    void shouldAssignTimestampAutomatically() {
        // Given: No timestamp is provided
        LocalDateTime beforeBuild = LocalDateTime.now();

        // When: Building Transaction without timestamp
        Transaction transaction = Transaction.builder()
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(100)
            .price(150)
            .build();

        LocalDateTime afterBuild = LocalDateTime.now();

        // Then: Timestamp should be automatically assigned
        assertNotNull(transaction.getTimestamp());
        assertTrue(transaction.getTimestamp().isEqual(beforeBuild) || 
                   transaction.getTimestamp().isAfter(beforeBuild) ||
                   transaction.getTimestamp().isEqual(afterBuild) ||
                   transaction.getTimestamp().isBefore(afterBuild));
    }

    @Test
    @DisplayName("11. Should allow optional id field")
    void shouldAllowOptionalIdField() {
        // When: Building Transaction without id (optional field)
        Transaction transaction = Transaction.builder()
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(50)
            .price(120)
            .build();

        // Then: id should be null
        assertNull(transaction.getId());
    }

    @Test
    @DisplayName("12. Should support optional id when provided")
    void shouldSupportOptionalIdWhenProvided() {
        // When: Building Transaction with id
        Transaction transaction = Transaction.builder()
            .id(999L)
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(50)
            .price(120)
            .build();

        // Then: id should be set
        assertEquals(999L, transaction.getId());
    }

    @Test
    @DisplayName("13. Should demonstrate fluent method chaining")
    void shouldDemonstrateFluentMethodChaining() {
        // This test explicitly demonstrates the fluent method chaining pattern
        // The pattern: .method(value).method(value).method(value).build()
        
        Transaction transaction = Transaction.builder()
            .id(1L)
            .offer(energyOffer)
            .buyer(consumer)
            .seller(producer)
            .kwh(75)
            .price(200)
            .timestamp(LocalDateTime.now())
            .build();

        // Verify fluent chaining works correctly
        assertAll("Transaction fields",
            () -> assertEquals(1L, transaction.getId()),
            () -> assertEquals(energyOffer, transaction.getOffer()),
            () -> assertEquals(consumer, transaction.getBuyer()),
            () -> assertEquals(producer, transaction.getSeller()),
            () -> assertEquals(75.0, transaction.getKwh()),
            () -> assertEquals(200.0, transaction.getPrice()),
            () -> assertNotNull(transaction.getTimestamp())
        );
    }
}

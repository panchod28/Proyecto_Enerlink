package com.enerlink.enerlink.energia.dominio.modelo;

import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnergyOffer Prototype Pattern Tests")
class EnergyOfferPrototypeTest {

    private User producer;
    private EnergyOffer originalOffer;

    @BeforeEach
    void setUp() {
        producer = new ProducerUser("Solar Producer", "solar@enerlink.com");
        originalOffer = new EnergyOffer(1L, producer, 100.0, 0.12, SaleType.DIRECT);
    }

    @Nested
    @DisplayName("Clone Method Tests")
    class CloneMethodTests {

        @Test
        @DisplayName("1. Should create a deep clone with clone()")
        void shouldCreateDeepClone() {
            EnergyOffer cloned = originalOffer.clone();

            assertNotNull(cloned);
            assertEquals(originalOffer.getId(), cloned.getId());
            assertEquals(originalOffer.getKwh(), cloned.getKwh(), 0.001);
            assertEquals(originalOffer.getPrice(), cloned.getPrice(), 0.001);
            assertEquals(originalOffer.getSaleType(), cloned.getSaleType());
        }

        @Test
        @DisplayName("2. Clone should be independent from original")
        void cloneShouldBeIndependent() {
            EnergyOffer cloned = originalOffer.clone();

            cloned.setId(999L);
            cloned.setKwh(500.0);
            cloned.setPrice(0.25);
            cloned.setSaleType(SaleType.AUCTION);

            assertEquals(1L, originalOffer.getId());
            assertEquals(100.0, originalOffer.getKwh(), 0.001);
            assertEquals(0.12, originalOffer.getPrice(), 0.001);
            assertEquals(SaleType.DIRECT, originalOffer.getSaleType());

            assertEquals(999L, cloned.getId());
            assertEquals(500.0, cloned.getKwh(), 0.001);
            assertEquals(0.25, cloned.getPrice(), 0.001);
            assertEquals(SaleType.AUCTION, cloned.getSaleType());
        }

        @Test
        @DisplayName("3. Clone should share producer reference (shallow aspect)")
        void cloneShouldShareProducerReference() {
            EnergyOffer cloned = originalOffer.clone();

            assertSame(originalOffer.getProducer(), cloned.getProducer());
        }

        @Test
        @DisplayName("4. Clone should not be the same instance as original")
        void cloneShouldNotBeSameInstance() {
            EnergyOffer cloned = originalOffer.clone();

            assertNotSame(originalOffer, cloned);
        }
    }

    @Nested
    @DisplayName("Shallow Clone Method Tests")
    class ShallowCloneMethodTests {

        @Test
        @DisplayName("5. Should create shallow clone with shallowClone()")
        void shouldCreateShallowClone() {
            EnergyOffer shallow = originalOffer.shallowClone();

            assertNotNull(shallow);
            assertEquals(originalOffer.getId(), shallow.getId());
            assertEquals(originalOffer.getKwh(), shallow.getKwh(), 0.001);
            assertEquals(originalOffer.getPrice(), shallow.getPrice(), 0.001);
            assertEquals(originalOffer.getSaleType(), shallow.getSaleType());
        }

        @Test
        @DisplayName("6. Shallow clone should not be same instance")
        void shallowCloneShouldNotBeSameInstance() {
            EnergyOffer shallow = originalOffer.shallowClone();

            assertNotSame(originalOffer, shallow);
        }

        @Test
        @DisplayName("7. Shallow clone should share producer reference")
        void shallowCloneShouldShareProducerReference() {
            EnergyOffer shallow = originalOffer.shallowClone();

            assertSame(originalOffer.getProducer(), shallow.getProducer());
        }
    }

    @Nested
    @DisplayName("Copy Constructor Tests")
    class CopyConstructorTests {

        @Test
        @DisplayName("8. Copy constructor should create valid copy")
        void copyConstructorShouldCreateValidCopy() {
            EnergyOffer copy = new EnergyOffer(originalOffer);

            assertEquals(originalOffer.getId(), copy.getId());
            assertEquals(originalOffer.getProducer(), copy.getProducer());
            assertEquals(originalOffer.getKwh(), copy.getKwh(), 0.001);
            assertEquals(originalOffer.getPrice(), copy.getPrice(), 0.001);
            assertEquals(originalOffer.getSaleType(), copy.getSaleType());
        }

        @Test
        @DisplayName("9. Copy constructor should throw exception for null input")
        void copyConstructorShouldThrowForNull() {
            assertThrows(IllegalArgumentException.class, () -> new EnergyOffer(null));
        }
    }

    @Nested
    @DisplayName("Clone With Modifications Tests")
    class CloneWithModificationsTests {

        @Test
        @DisplayName("10. Should clone with new ID")
        void shouldCloneWithNewId() {
            EnergyOffer cloned = originalOffer.cloneWithNewId(2L);

            assertEquals(2L, cloned.getId());
            assertEquals(originalOffer.getProducer(), cloned.getProducer());
            assertEquals(originalOffer.getKwh(), cloned.getKwh(), 0.001);
            assertEquals(originalOffer.getPrice(), cloned.getPrice(), 0.001);
            assertEquals(originalOffer.getSaleType(), cloned.getSaleType());
        }

        @Test
        @DisplayName("11. Should clone with new price")
        void shouldCloneWithNewPrice() {
            EnergyOffer cloned = originalOffer.cloneWithNewPrice(0.15);

            assertEquals(1L, cloned.getId());
            assertEquals(0.15, cloned.getPrice(), 0.001);
            assertEquals(originalOffer.getKwh(), cloned.getKwh(), 0.001);
        }

        @Test
        @DisplayName("12. Should clone with new kWh")
        void shouldCloneWithNewKwh() {
            EnergyOffer cloned = originalOffer.cloneWithNewKwh(250.0);

            assertEquals(250.0, cloned.getKwh(), 0.001);
            assertEquals(originalOffer.getPrice(), cloned.getPrice(), 0.001);
        }

        @Test
        @DisplayName("13. Should clone with new sale type")
        void shouldCloneWithNewSaleType() {
            EnergyOffer cloned = originalOffer.cloneWithNewSaleType(SaleType.AUCTION);

            assertEquals(SaleType.AUCTION, cloned.getSaleType());
            assertEquals(SaleType.DIRECT, originalOffer.getSaleType());
        }
    }

    @Nested
    @DisplayName("Prototype Interface Implementation Tests")
    class PrototypeInterfaceTests {

        @Test
        @DisplayName("14. EnergyOffer should implement Prototype interface")
        void shouldImplementPrototypeInterface() {
            assertTrue(originalOffer instanceof Prototype);
        }

        @Test
        @DisplayName("15. Clone should return EnergyOffer type")
        void cloneShouldReturnEnergyOfferType() {
            Prototype<EnergyOffer> prototype = originalOffer;
            EnergyOffer cloned = prototype.clone();

            assertInstanceOf(EnergyOffer.class, cloned);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("16. Should handle null producer in original offer")
        void shouldHandleNullProducer() {
            EnergyOffer offerWithNullProducer = new EnergyOffer(1L, null, 100.0, 0.12, SaleType.DIRECT);
            EnergyOffer cloned = offerWithNullProducer.clone();

            assertNull(cloned.getProducer());
        }

        @Test
        @DisplayName("17. Should handle null ID in original offer")
        void shouldHandleNullId() {
            EnergyOffer offerWithNullId = new EnergyOffer(null, producer, 100.0, 0.12, SaleType.DIRECT);
            EnergyOffer cloned = offerWithNullId.clone();

            assertNull(cloned.getId());
        }

        @Test
        @DisplayName("18. Should handle zero values")
        void shouldHandleZeroValues() {
            EnergyOffer offerWithZeros = new EnergyOffer(1L, producer, 0.0, 0.0, SaleType.DIRECT);
            EnergyOffer cloned = offerWithZeros.clone();

            assertEquals(0.0, cloned.getKwh(), 0.001);
            assertEquals(0.0, cloned.getPrice(), 0.001);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("19. toString should contain all fields")
        void toStringShouldContainAllFields() {
            String result = originalOffer.toString();

            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("kwh=100.0"));
            assertTrue(result.contains("price=0.12"));
            assertTrue(result.contains("saleType=DIRECT"));
        }
    }

}

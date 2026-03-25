package com.enerlink.enerlink.energia.dominio.factory;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.energia.dominio.modelo.SaleType;
import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnergyOfferPrototypeRegistry Tests")
class EnergyOfferPrototypeRegistryTest {

    private EnergyOfferPrototypeRegistry registry;
    private User producer;
    private EnergyOffer standardOffer;

    @BeforeEach
    void setUp() {
        registry = EnergyOfferPrototypeRegistry.getInstance();
        registry.clear();
        
        producer = new ProducerUser("Wind Farm", "wind@enerlink.com");
        standardOffer = new EnergyOffer(null, producer, 100.0, 0.10, SaleType.DIRECT);
    }

    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonTests {

        @Test
        @DisplayName("1. getInstance should return same instance")
        void getInstanceShouldReturnSameInstance() {
            EnergyOfferPrototypeRegistry instance1 = EnergyOfferPrototypeRegistry.getInstance();
            EnergyOfferPrototypeRegistry instance2 = EnergyOfferPrototypeRegistry.getInstance();

            assertSame(instance1, instance2);
        }
    }

    @Nested
    @DisplayName("Register and Unregister Tests")
    class RegisterTests {

        @Test
        @DisplayName("2. Should register a prototype")
        void shouldRegisterPrototype() {
            registry.register("standard-direct", standardOffer);

            assertTrue(registry.exists("standard-direct"));
        }

        @Test
        @DisplayName("3. Should unregister a prototype")
        void shouldUnregisterPrototype() {
            registry.register("to-remove", standardOffer);
            registry.unregister("to-remove");

            assertFalse(registry.exists("to-remove"));
        }

        @Test
        @DisplayName("4. Should throw exception for null key on register")
        void shouldThrowExceptionForNullKey() {
            assertThrows(IllegalArgumentException.class, 
                () -> registry.register(null, standardOffer));
        }

        @Test
        @DisplayName("5. Should throw exception for blank key on register")
        void shouldThrowExceptionForBlankKey() {
            assertThrows(IllegalArgumentException.class, 
                () -> registry.register("  ", standardOffer));
        }

        @Test
        @DisplayName("6. Should throw exception for null prototype on register")
        void shouldThrowExceptionForNullPrototype() {
            assertThrows(IllegalArgumentException.class, 
                () -> registry.register("valid-key", null));
        }
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("7. Should create clone from registered prototype")
        void shouldCreateCloneFromPrototype() {
            registry.register("my-offer", standardOffer);
            
            EnergyOffer created = registry.create("my-offer");

            assertNotNull(created);
            assertNotSame(standardOffer, created);
            assertEquals(standardOffer.getKwh(), created.getKwh(), 0.001);
            assertEquals(standardOffer.getPrice(), created.getPrice(), 0.001);
        }

        @Test
        @DisplayName("8. Created clone should be independent")
        void createdCloneShouldBeIndependent() {
            registry.register("my-offer", standardOffer);
            
            EnergyOffer created = registry.create("my-offer");
            created.setId(1L);
            created.setKwh(500.0);

            assertNull(standardOffer.getId());
            assertEquals(100.0, standardOffer.getKwh(), 0.001);
        }

        @Test
        @DisplayName("9. Should throw UnknownPrototypeException for unknown key")
        void shouldThrowExceptionForUnknownKey() {
            assertThrows(EnergyOfferPrototypeRegistry.UnknownPrototypeException.class,
                () -> registry.create("non-existent-key"));
        }
    }

    @Nested
    @DisplayName("Create With ID Tests")
    class CreateWithIdTests {

        @Test
        @DisplayName("10. Should create clone with new ID")
        void shouldCreateCloneWithNewId() {
            registry.register("my-offer", standardOffer);
            
            EnergyOffer created = registry.createWithId("my-offer", 42L);

            assertEquals(42L, created.getId());
            assertEquals(standardOffer.getKwh(), created.getKwh(), 0.001);
        }
    }

    @Nested
    @DisplayName("Create With Producer Tests")
    class CreateWithProducerTests {

        @Test
        @DisplayName("11. Should create clone with new producer")
        void shouldCreateCloneWithNewProducer() {
            registry.register("my-offer", standardOffer);
            User newProducer = new ProducerUser("New Solar", "new@enerlink.com");
            
            EnergyOffer created = registry.createWithProducer("my-offer", newProducer);

            assertSame(newProducer, created.getProducer());
            assertEquals(standardOffer.getKwh(), created.getKwh(), 0.001);
        }
    }

    @Nested
    @DisplayName("Create With Modifications Tests")
    class CreateWithModificationsTests {

        @Test
        @DisplayName("12. Should create clone with custom modifications")
        void shouldCreateCloneWithModifications() {
            registry.register("my-offer", standardOffer);
            
            EnergyOffer created = registry.createWithModifications("my-offer", offer -> {
                offer.setId(100L);
                offer.setKwh(200.0);
                offer.setPrice(0.15);
            });

            assertEquals(100L, created.getId());
            assertEquals(200.0, created.getKwh(), 0.001);
            assertEquals(0.15, created.getPrice(), 0.001);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityTests {

        @Test
        @DisplayName("13. exists should return true for registered key")
        void existsShouldReturnTrueForRegisteredKey() {
            registry.register("my-offer", standardOffer);

            assertTrue(registry.exists("my-offer"));
        }

        @Test
        @DisplayName("14. exists should return false for unregistered key")
        void existsShouldReturnFalseForUnregisteredKey() {
            assertFalse(registry.exists("non-existent"));
        }

        @Test
        @DisplayName("15. getRegisteredKeys should return all keys")
        void getRegisteredKeysShouldReturnAllKeys() {
            registry.register("offer-1", standardOffer);
            registry.register("offer-2", standardOffer);

            Set<String> keys = registry.getRegisteredKeys();

            assertEquals(2, keys.size());
            assertTrue(keys.contains("offer-1"));
            assertTrue(keys.contains("offer-2"));
        }

        @Test
        @DisplayName("16. getPrototypeCount should return correct count")
        void getPrototypeCountShouldReturnCorrectCount() {
            assertEquals(0, registry.getPrototypeCount());

            registry.register("offer-1", standardOffer);
            assertEquals(1, registry.getPrototypeCount());

            registry.register("offer-2", standardOffer);
            assertEquals(2, registry.getPrototypeCount());
        }

        @Test
        @DisplayName("17. clear should remove all prototypes")
        void clearShouldRemoveAllPrototypes() {
            registry.register("offer-1", standardOffer);
            registry.register("offer-2", standardOffer);
            
            registry.clear();

            assertEquals(0, registry.getPrototypeCount());
            assertTrue(registry.getRegisteredKeys().isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("18. Should support template-based offer creation workflow")
        void shouldSupportTemplateBasedWorkflow() {
            EnergyOffer solarTemplate = new EnergyOffer(null, producer, 100.0, 0.10, SaleType.DIRECT);
            registry.register("solar-standard", solarTemplate);

            EnergyOffer offer1 = registry.createWithId("solar-standard", 1L);
            offer1.setPrice(0.09);

            EnergyOffer offer2 = registry.createWithId("solar-standard", 2L);
            offer2.setPrice(0.11);

            EnergyOffer offer3 = registry.createWithId("solar-standard", 3L);

            assertNotSame(offer1, offer2);
            assertNotSame(offer2, offer3);
            assertEquals(1L, offer1.getId());
            assertEquals(2L, offer2.getId());
            assertEquals(3L, offer3.getId());
            assertEquals(0.09, offer1.getPrice(), 0.001);
            assertEquals(0.11, offer2.getPrice(), 0.001);
            assertEquals(0.10, offer3.getPrice(), 0.001);
        }

        @Test
        @DisplayName("19. Should support auction template with different sale type")
        void shouldSupportAuctionTemplate() {
            EnergyOffer auctionTemplate = new EnergyOffer(null, producer, 200.0, 0.08, SaleType.AUCTION);
            registry.register("auction-large", auctionTemplate);

            EnergyOffer auctionOffer = registry.createWithModifications("auction-large", offer -> {
                offer.setId(10L);
                offer.setKwh(500.0);
            });

            assertEquals(10L, auctionOffer.getId());
            assertEquals(500.0, auctionOffer.getKwh(), 0.001);
            assertEquals(0.08, auctionOffer.getPrice(), 0.001);
            assertEquals(SaleType.AUCTION, auctionOffer.getSaleType());
        }
    }

}

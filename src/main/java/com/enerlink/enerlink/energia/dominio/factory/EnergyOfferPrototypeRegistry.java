package com.enerlink.enerlink.energia.dominio.factory;

import com.enerlink.enerlink.energia.dominio.modelo.EnergyOffer;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for managing EnergyOffer prototypes.
 * 
 * Implements the Singleton pattern to ensure only one instance exists,
 * combined with the Prototype pattern to provide template management.
 * 
 * This registry allows:
 * - Registering predefined offer templates (prototypes)
 * - Cloning prototypes to create new offers
 * - Managing common offer configurations centrally
 * 
 * Example usage:
 * <pre>
 * // Register a standard direct sale template
 * EnergyOffer standardOffer = new EnergyOffer(null, producer, 100.0, 0.10, SaleType.DIRECT);
 * EnergyOfferPrototypeRegistry.getInstance().register("standard-direct", standardOffer);
 * 
 * // Create new offers by cloning the template
 * EnergyOffer newOffer = EnergyOfferPrototypeRegistry.getInstance().create("standard-direct");
 * newOffer.setId(1L);
 * </pre>
 */
public final class EnergyOfferPrototypeRegistry {

    private static final EnergyOfferPrototypeRegistry INSTANCE = new EnergyOfferPrototypeRegistry();

    private final Map<String, EnergyOffer> prototypes;

    private EnergyOfferPrototypeRegistry() {
        this.prototypes = new HashMap<>();
        initializeDefaultPrototypes();
    }

    /**
     * Returns the singleton instance of the registry.
     *
     * @return the singleton instance
     */
    public static EnergyOfferPrototypeRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes default prototype templates.
     * Subclasses can override this to customize default prototypes.
     */
    protected void initializeDefaultPrototypes() {
    }

    /**
     * Registers a prototype with the given key.
     *
     * @param key       the unique identifier for the prototype
     * @param prototype the EnergyOffer prototype to register
     * @throws IllegalArgumentException if key or prototype is null
     */
    public void register(String key, EnergyOffer prototype) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Prototype key cannot be null or blank");
        }
        if (prototype == null) {
            throw new IllegalArgumentException("Prototype cannot be null");
        }
        prototypes.put(key, prototype);
    }

    /**
     * Unregisters a prototype by its key.
     *
     * @param key the key of the prototype to remove
     */
    public void unregister(String key) {
        prototypes.remove(key);
    }

    /**
     * Creates a new EnergyOffer by cloning the prototype registered with the given key.
     *
     * @param key the key of the prototype to clone
     * @return a new EnergyOffer instance cloned from the prototype
     * @throws UnknownPrototypeException if no prototype exists with the given key
     */
    public EnergyOffer create(String key) {
        EnergyOffer prototype = prototypes.get(key);
        if (prototype == null) {
            throw new UnknownPrototypeException("No prototype registered with key: " + key);
        }
        return prototype.clone();
    }

    /**
     * Creates a new EnergyOffer by cloning the prototype with a new ID assigned.
     *
     * @param key   the key of the prototype to clone
     * @param newId the ID to assign to the cloned offer
     * @return a new EnergyOffer instance with the specified ID
     * @throws UnknownPrototypeException if no prototype exists with the given key
     */
    public EnergyOffer createWithId(String key, Long newId) {
        EnergyOffer clone = create(key);
        clone.setId(newId);
        return clone;
    }

    /**
     * Creates a new EnergyOffer by cloning the prototype with a new producer assigned.
     *
     * @param key          the key of the prototype to clone
     * @param newProducer  the new producer for the cloned offer
     * @return a new EnergyOffer instance with the specified producer
     * @throws UnknownPrototypeException if no prototype exists with the given key
     */
    public EnergyOffer createWithProducer(String key, User newProducer) {
        EnergyOffer clone = create(key);
        clone.setProducer(newProducer);
        return clone;
    }

    /**
     * Creates a new EnergyOffer by cloning the prototype with custom modifications.
     *
     * @param key          the key of the prototype to clone
     * @param modifier      functional interface to modify the cloned offer
     * @return a new EnergyOffer instance with the modifications applied
     * @throws UnknownPrototypeException if no prototype exists with the given key
     */
    public EnergyOffer createWithModifications(String key, PrototypeModifier modifier) {
        EnergyOffer clone = create(key);
        modifier.modify(clone);
        return clone;
    }

    /**
     * Checks if a prototype exists with the given key.
     *
     * @param key the key to check
     * @return true if a prototype exists, false otherwise
     */
    public boolean exists(String key) {
        return prototypes.containsKey(key);
    }

    /**
     * Returns all registered prototype keys.
     *
     * @return set of all registered keys
     */
    public Set<String> getRegisteredKeys() {
        return Set.copyOf(prototypes.keySet());
    }

    /**
     * Gets the number of registered prototypes.
     *
     * @return the count of prototypes
     */
    public int getPrototypeCount() {
        return prototypes.size();
    }

    /**
     * Clears all registered prototypes.
     * Use with caution in production environments.
     */
    public void clear() {
        prototypes.clear();
    }

    /**
     * Functional interface for modifying cloned prototypes.
     */
    @FunctionalInterface
    public interface PrototypeModifier {
        void modify(EnergyOffer offer);
    }

    /**
     * Exception thrown when a requested prototype key is not found.
     */
    public static class UnknownPrototypeException extends RuntimeException {
        public UnknownPrototypeException(String message) {
            super(message);
        }
    }

}

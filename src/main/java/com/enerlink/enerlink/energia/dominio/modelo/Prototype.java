package com.enerlink.enerlink.energia.dominio.modelo;

/**
 * Prototype interface for the Prototype Design Pattern.
 * 
 * Defines the contract for objects that can clone themselves.
 * This pattern is used when object creation is expensive or when
 * we need to create copies of objects without coupling to their concrete classes.
 */
public interface Prototype<T> {

    /**
     * Creates a deep copy of this object.
     * 
     * @return a new instance that is a copy of this object
     * @throws CloneNotSupportedException if cloning is not supported
     */
    T clone();

    /**
     * Creates a shallow copy of this object.
     * Shares references to mutable objects with the original.
     * 
     * @return a new instance with shallow copied references
     */
    T shallowClone();

}

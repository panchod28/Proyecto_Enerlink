package com.enerlink.enerlink.usuario.dominio.modelo;

/**
 * Usuario productor de energía.
 * Producto concreto del patrón Factory Method.
 */
public class ProducerUser extends User {

    public ProducerUser(String nombre, String email) {
        super(nombre, email, "PRODUCER");
    }

    @Override
    public String describir() {
        return "Soy " + nombre + " y produzco energía en Enerlink.";
    }
}

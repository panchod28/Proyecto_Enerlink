package com.enerlink.enerlink.usuario.dominio.modelo;

/**
 * Usuario consumidor de energía.
 * Producto concreto del patrón Factory Method.
 */
public class ConsumerUser extends User {

    public ConsumerUser(String nombre, String email) {
        super(nombre, email, "CONSUMER");
    }

    @Override
    public String describir() {
        return "Soy " + nombre + " y consumo energía en Enerlink.";
    }
}
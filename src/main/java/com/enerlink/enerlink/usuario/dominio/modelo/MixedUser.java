package com.enerlink.enerlink.usuario.dominio.modelo;

/**
 * Usuario consumidor de energía.
 * Producto concreto del patrón Factory Method.
 */
public class MixedUser extends User {

    public MixedUser(String nombre, String email) {
        super(nombre, email, "MIXED");
    }

    @Override
    public String describir() {
        return "Soy " + nombre + ", Produzco y consumo energía en Enerlink.";
    }
}
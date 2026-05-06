package com.enerlink.enerlink.usuario.dominio.modelo;

/**
 * Usuario administrador del sistema.
 * Producto concreto del patrón Factory Method.
 */
public class AdminUser extends User {

    public AdminUser(String nombre, String email) {
        super(nombre, email, "ADMIN");
    }

    @Override
    public String describir() {
        return "Soy " + nombre + " y administro Enerlink.";
    }
}

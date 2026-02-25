package com.enerlink.enerlink.usuario.dominio.factory;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.usuario.dominio.modelo.MixedUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * Factory concreto que crea usuarios mixtos (producen y consumen energía).
 * Producto concreto del patrón Factory Method.
 */
@Component("MIXED")
public class MixedUserFactory extends UserFactory {

    @Override
    public User crearUsuario(String nombre, String email) {
        return new MixedUser(nombre, email);
    }
}
package com.enerlink.enerlink.usuario.dominio.factory;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.usuario.dominio.modelo.ProducerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * Factory concreto que crea usuarios productores de energía.
 * Producto concreto del patrón Factory Method.
 */
@Component("PRODUCER")
public class ProducerUserFactory extends UserFactory {

    @Override
    public User crearUsuario(String nombre, String email) {
        return new ProducerUser(nombre, email);
    }
}
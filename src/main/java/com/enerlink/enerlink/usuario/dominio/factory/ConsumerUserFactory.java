package com.enerlink.enerlink.usuario.dominio.factory;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.usuario.dominio.modelo.ConsumerUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * Factory concreto que crea usuarios consumidores de energía.
 * Producto concreto del patrón Factory Method.
 */
@Component("CONSUMER")
public class ConsumerUserFactory extends UserFactory {

    @Override
    public User crearUsuario(String nombre, String email) {
        return new ConsumerUser(nombre, email);
    }
}
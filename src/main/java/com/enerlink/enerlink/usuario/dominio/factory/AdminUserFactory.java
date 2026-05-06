package com.enerlink.enerlink.usuario.dominio.factory;

import org.springframework.stereotype.Component;

import com.enerlink.enerlink.usuario.dominio.modelo.AdminUser;
import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * Factory concreto que crea usuarios administradores.
 * Producto concreto del patrón Factory Method.
 */
@Component("ADMIN")
public class AdminUserFactory extends UserFactory {

    @Override
    public User crearUsuario(String nombre, String email) {
        return new AdminUser(nombre, email);
    }
}

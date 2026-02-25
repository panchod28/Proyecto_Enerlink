package com.enerlink.enerlink.usuario.dominio.factory;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

/**
 * FACTORY METHOD CLÁSICO
 *
 * Clase abstracta que define el contrato de cualquier Factory de usuarios.
 * Cada subclase concreta decide qué tipo de usuario crear.
 */
public abstract class UserFactory {

    /**
     * Método factory abstracto: cada subclase concreta implementa la creación de su usuario.
     *
     * @param nombre nombre del usuario
     * @param email  correo del usuario
     * @return instancia concreta de User
     */
    public abstract User crearUsuario(String nombre, String email);

}
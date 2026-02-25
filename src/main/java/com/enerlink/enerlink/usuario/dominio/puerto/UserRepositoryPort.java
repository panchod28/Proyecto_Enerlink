package com.enerlink.enerlink.usuario.dominio.puerto;

import com.enerlink.enerlink.usuario.dominio.modelo.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio.
 * Define las operaciones necesarias para persistencia.
 *
 * El dominio NO conoce la implementación concreta.
 */
public interface UserRepositoryPort {

    User guardar(User user);

    Optional<User> buscarPorId(Long id);

    List<User> listarTodos();

    void eliminarPorId(Long id);
}
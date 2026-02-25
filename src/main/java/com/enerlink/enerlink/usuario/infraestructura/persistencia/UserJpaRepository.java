package com.enerlink.enerlink.usuario.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA.
 * Solo lo conoce el adaptador, nunca el dominio.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
}
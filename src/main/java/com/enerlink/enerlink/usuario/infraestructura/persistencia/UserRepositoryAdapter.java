package com.enerlink.enerlink.usuario.infraestructura.persistencia;

import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia.
 *
 * Convierte entre dominio (User) y entidad JPA (UserEntity).
 * Es la única clase que conoce JPA.
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User guardar(User user) {

        UserEntity entity = new UserEntity(
                user.getNombre(),
                user.getEmail(),
                user.getRol()
        );

        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        UserEntity guardada = jpaRepository.save(entity);

        user.setId(guardada.getId());
        return user;
    }

    @Override
    public Optional<User> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(entity -> {
                    User user = new User(
                            entity.getNombre(),
                            entity.getEmail(),
                            entity.getRol()
                    ) {
                    };
                    user.setId(entity.getId());
                    return user;
                });
    }

    @Override
    public List<User> listarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(entity -> {
                    User user = new User(
                            entity.getNombre(),
                            entity.getEmail(),
                            entity.getRol()
                    ) {
                        @Override
                        public String describir() {
                            return "";
                        }
                    };
                    user.setId(entity.getId());
                    return user;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorId(Long id) {
        jpaRepository.deleteById(id);
    }
}
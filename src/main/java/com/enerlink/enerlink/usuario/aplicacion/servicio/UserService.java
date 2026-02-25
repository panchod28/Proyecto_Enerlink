package com.enerlink.enerlink.usuario.aplicacion.servicio;

import com.enerlink.enerlink.configuracion.PredictionEngine;
import com.enerlink.enerlink.usuario.dominio.factory.UserFactory;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import com.enerlink.enerlink.usuario.dominio.puerto.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio de aplicación. Orquesta los casos de uso de usuarios.
 *
 * Características:
 * - No usa "new User(...)" → delega en factories específicas (Factory Method
 * clásico)
 * - La capa de persistencia devuelve datos planos → el Service reconstruye los
 * tipos polimórficos
 * - Predicción de consumo usando PredictionEngine (Singleton)
 * - Limpio, extensible y siguiendo SOLID
 */
@Service
public class UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final Map<String, UserFactory> userFactories;

    /**
     * Inyección de dependencias:
     * - Repositorio (puerto de salida)
     * - Map de factories por rol (puede inyectarse automáticamente con Spring)
     */
    public UserService(UserRepositoryPort userRepositoryPort, Map<String, UserFactory> userFactories) {
        this.userRepositoryPort = userRepositoryPort;
        this.userFactories = userFactories;
    }

    /**
     * Caso de uso: crear un nuevo usuario.
     * Usa el Factory Method para instanciar la subclase correcta según el rol.
     */
    public User crearUsuario(String nombre, String email, String rol) {
        UserFactory factory = obtenerFactory(rol);
        User user = factory.crearUsuario(nombre, email);
        // SINGLETON: predicción de consumo
        double prediccion = PredictionEngine.INSTANCE.predict(100.0);
        System.out.println("Predicción de consumo para nuevo usuario: " + prediccion + " kWh");
        return userRepositoryPort.guardar(user);
    }

    public User actualizarUsuario(Long id, String nombre, String email, String rol) {
        UserFactory factory = obtenerFactory(rol);
        User user = factory.crearUsuario(nombre, email);
        user.setId(id);
        return userRepositoryPort.guardar(user);
    }

    public User obtenerUsuarioPorId(Long id) {
        User plano = userRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return reconstruirPolimorfico(plano);
    }

    public List<User> listarUsuarios() {
        return userRepositoryPort.listarTodos()
                .stream()
                .map(this::reconstruirPolimorfico)
                .toList();
    }

    public void eliminarUsuario(Long id) {
        userRepositoryPort.eliminarPorId(id);
    }

    private User reconstruirPolimorfico(User plano) {
        UserFactory factory = obtenerFactory(plano.getRol());
        User user = factory.crearUsuario(plano.getNombre(), plano.getEmail());
        user.setId(plano.getId());
        return user;
    }

    private UserFactory obtenerFactory(String rol) {
        UserFactory factory = userFactories.get(rol.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Rol desconocido: " + rol);
        }
        return factory;
    }
}
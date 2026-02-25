package com.enerlink.enerlink.usuario.infraestructura.controlador;

import com.enerlink.enerlink.usuario.aplicacion.servicio.UserService;
import com.enerlink.enerlink.usuario.dominio.modelo.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST. Adaptador de entrada (Driving Adapter).
 *
 * Recibe las peticiones HTTP y las delega al servicio de aplicación.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User crear(@RequestBody UserRequest request) {
        return userService.crearUsuario(
                request.getNombre(),
                request.getEmail(),
                request.getRol());
    }

    @GetMapping
    public List<User> listar() {
        return userService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public User obtener(@PathVariable Long id) {
        return userService.obtenerUsuarioPorId(id);
    }

    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id,
            @RequestBody UserRequest request) {
        return userService.actualizarUsuario(
                id,
                request.getNombre(),
                request.getEmail(),
                request.getRol());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        userService.eliminarUsuario(id);
    }
}
package com.enerlink.enerlink.usuario.infraestructura.persistencia;

import jakarta.persistence.*;

/**
 * Entidad JPA. Pertenece a la infraestructura, no al dominio.
 * Mapea la tabla "users" en H2.
 *
 * El dominio NO conoce esta clase. Solo el adaptador la usa.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String rol;

    // Constructor vacío requerido por JPA
    public UserEntity() {}

    public UserEntity(String nombre, String email, String rol) {
        this.nombre = nombre;
        this.email  = email;
        this.rol    = rol;
    }

    // Getters y Setters
    public Long getId()       { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }
    public String getRol()    { return rol; }

    public void setId(Long id)          { this.id = id; }
    public void setNombre(String nombre){ this.nombre = nombre; }
    public void setEmail(String email)  { this.email = email; }
    public void setRol(String rol)      { this.rol = rol; }
}
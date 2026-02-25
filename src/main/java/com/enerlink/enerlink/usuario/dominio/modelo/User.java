package com.enerlink.enerlink.usuario.dominio.modelo;

/**
 * Clase abstracta del dominio. No depende de Spring.
 * Define el contrato común de cualquier tipo de usuario en Enerlink.
 */
public abstract class User {

    protected Long id;
    protected String nombre;
    protected String email;
    protected String rol; // "PRODUCER" o "CONSUMER"

    public User(String nombre, String email, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.rol   = rol;
    }

    public String describir() {
        return "Usuario: " + nombre + " con rol: " + rol;
    }

    // Getters
    public Long getId()       { return id; }
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }
    public String getRol()    { return rol; }

    public void setId(Long id) { this.id = id; }
}
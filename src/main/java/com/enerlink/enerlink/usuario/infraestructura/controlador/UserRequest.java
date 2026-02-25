package com.enerlink.enerlink.usuario.infraestructura.controlador;

/**
 * DTO (Data Transfer Object) para recibir el JSON del cliente.
 * Pertenece a la infraestructura. El dominio no lo conoce.
 */
public class UserRequest {
    private String nombre;
    private String email;
    private String rol;

    // Getters y Setters
    public String getNombre() { return nombre; }
    public String getEmail()  { return email; }
    public String getRol()    { return rol; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email)   { this.email = email; }
    public void setRol(String rol)       { this.rol = rol; }
}
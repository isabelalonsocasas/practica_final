package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idRol;

    @NotNull(message = "El nombre del rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    public NombreRol nombreRol;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false)
    public String descripcion;

    public enum NombreRol {
        USER,
        ADMIN
    }

    public Rol() {
    }

    public Rol(NombreRol nombreRol, String descripcion) {
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }
}

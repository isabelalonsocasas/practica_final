package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    @NotNull(message = "El nombre del rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private NombreRol nombreRol;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Column(nullable = false)
    private String descripcion;

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

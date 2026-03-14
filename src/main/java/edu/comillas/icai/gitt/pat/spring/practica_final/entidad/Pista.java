package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idPista;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    public String nombre;

    @NotBlank(message = "La ubicación no puede estar vacía")
    @Column(nullable = false)
    public String ubicacion;

    @Positive(message = "El precio por hora debe ser positivo")
    @Column(nullable = false)
    public double precioHora;

    @Column(nullable = false)
    public boolean activa;

    @NotNull
    @Column(nullable = false)
    public LocalDateTime fechaAlta;

    public Pista() {
        if (this.fechaAlta == null) {
            this.fechaAlta = LocalDateTime.now();
        }
    }
}

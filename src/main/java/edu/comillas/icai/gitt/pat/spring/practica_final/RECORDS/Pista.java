package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record Pista(
    @Positive @NotNull
    int idPista,

    @NotBlank
    String nombre,

    @NotBlank
    String ubicacion,

    @Positive
    double precioHora,

    boolean activa,

    @NotNull
    LocalDateTime fechaAlta

) {
    public Pista {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return activa;
    }

    public Object getIdPista() {
        return idPista;
    }

    public Object getNombre() {
        return nombre;
    }
}

package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record Rol(
        @Positive @NotNull
        int idRol,

        @NotNull
        NombreRol nombreRol,

        @NotBlank
        String descripcion
) {
    public enum NombreRol {
        USER,
        ADMIN
    }
}

package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record Usuario(

        @NotBlank
        String idUsuario,

        @NotBlank
        String nombre,

        @NotBlank
        String apellidos,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String telefono,

        @NotNull
        Rol rol,

        @NotNull
        LocalDateTime fechaRegistro,

        boolean activo,

        List<Reserva> reservas  // Lista de Reservas que tiene asignadas el usuario

) {

    public enum Rol { //Para que Rol solo pueda tomar valores USER o ADMIN
        USER,
        ADMIN
    }
}
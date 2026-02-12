package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public record Usuario(

        @Positive @NotNull
        int idUsuario,

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

        @NotNull //Como hemos creado uno por defecto pensar si es necesario
        Rol rol,

        @NotNull
        LocalDateTime fechaRegistro,

        boolean activo,

        List<Reserva> reservas  // Lista de Reservas que tiene asignadas el usuario

) {
        //LOS USUARIOS SERAN POR DEFECTO USER
        public Usuario{
                if (rol == null) {
                        rol = new Rol(
                                1,
                                Rol.NombreRol.USER,
                                "Usuario por defecto"
                        );
                }

                if (fechaRegistro == null) {
                        fechaRegistro = LocalDateTime.now();
                }
        }
}
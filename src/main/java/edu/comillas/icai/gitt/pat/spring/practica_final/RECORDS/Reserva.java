package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record Reserva(
        @Positive @NotNull
        int idReserva,

        @PositiveOrZero
        int idUsuario,

        @PositiveOrZero
        int idPista,

        @NotNull
        LocalDateTime fechaReserva,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        int duracionMinutos,

        LocalTime horaFin,

        @NotNull
        Estado estado,

        @NotNull
        LocalDateTime fechaCreacion
) {
    public enum Estado {
        ACTIVA,
        CANCELADA
    }

    public Reserva {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        
        if (horaFin == null) {
            horaFin = horaInicio().plusMinutes(duracionMinutos);
        }
    }
}

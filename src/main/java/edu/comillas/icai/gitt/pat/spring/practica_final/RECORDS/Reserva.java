package edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
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
        LocalDate fechaReserva,

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
    public Integer getIdPista() {
        return idPista;
    }

    public LocalDate getFecha() {
        return fechaReserva;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

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
    public Reserva(int idReserva, int idUsuario, int idPista, LocalDate fechaReserva, LocalTime horaInicio, int duracionMinutos) {
        this(
                idReserva,
                idUsuario,
                idPista,
                fechaReserva,
                horaInicio,
                duracionMinutos,
                null,
                Estado.ACTIVA,
                null
        );
    }
}

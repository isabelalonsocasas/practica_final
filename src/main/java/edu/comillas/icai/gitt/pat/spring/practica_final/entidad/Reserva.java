package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idReserva;

    @ManyToOne
    public Usuario usuario;

    @ManyToOne
    public Pista pista;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Column(nullable = false)
    public LocalDate fechaReserva;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    public LocalTime horaInicio;

    @Positive(message = "La duración debe ser positiva")
    @Column(nullable = false)
    public int duracionMinutos;

    public LocalTime horaFin;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Estado estado = Estado.ACTIVA;

    @Column(nullable = false)
    public LocalDateTime fechaCreacion;

    public enum Estado {
        ACTIVA,
        CANCELADA
    }

    public Reserva() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }

        if (this.horaFin == null && this.horaInicio != null) {
            this.horaFin = this.horaInicio.plusMinutes(this.duracionMinutos);
        }
    }

//    @PrePersist
//    public void calcularCamposPorDefecto() {
//        if (this.fechaCreacion == null) {
//            this.fechaCreacion = LocalDateTime.now();
//        }
//
//        if (this.horaFin == null && this.horaInicio != null) {
//            this.horaFin = this.horaInicio.plusMinutes(this.duracionMinutos);
//        }
//    }

}

package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @ManyToOne
    @JoinColumn(name="id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="id_pista")
    private Pista pista;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaReserva;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime horaInicio;

    @Positive(message = "La duración debe ser positiva")
    @Column(nullable = false)
    private Integer duracionMinutos;

    private LocalTime horaFin;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVA;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public enum Estado {
        ACTIVA,
        CANCELADA
    }

    @PrePersist
    public void Reserva() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }

        if (this.horaFin == null && this.horaInicio != null) {
            this.horaFin = this.horaInicio.plusMinutes(this.duracionMinutos);
        }
    }

}

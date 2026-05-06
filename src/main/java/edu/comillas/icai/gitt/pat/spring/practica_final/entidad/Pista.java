package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPista;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "La ubicación no puede estar vacía")
    @Column(nullable = false)
    private String ubicacion;

    @Positive(message = "El precio por hora debe ser positivo")
    @Column(nullable = false)
    private Double precioHora;

    @Column(nullable = false)
    private boolean activa;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaAlta;

    @OneToMany(mappedBy = "pista", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Reserva> reservas;


    public Pista() {
        if (this.fechaAlta == null) {
            this.fechaAlta = LocalDateTime.now();
        }
    }
}

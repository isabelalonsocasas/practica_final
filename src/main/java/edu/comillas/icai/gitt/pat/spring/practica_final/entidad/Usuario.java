package edu.comillas.icai.gitt.pat.spring.practica_final.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long idUsuario;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    public String nombre;

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Column(nullable = false)
    public String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato de email no es válido")
    @Column(nullable = false, unique = true) // unique = true es importante para un login
    public String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    public String password;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Column(nullable = false)
    public String telefono;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    public Rol rol;

    @Column(nullable = false)
    public LocalDateTime fechaRegistro;

    @Column(nullable = false)
    public boolean activo = true;

    @OneToMany
    public List<Reserva> reservas = new ArrayList<>();


    public Usuario() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

//    // Lógica antes de guardar en la base de datos
//    @PrePersist
//    public void inicializarCampos() {
//        if (this.fechaRegistro == null) {
//            this.fechaRegistro = LocalDateTime.now();
//        }
//    }
}
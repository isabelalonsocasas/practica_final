package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class AlmacenDatos
{

    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();
    private final Map<Integer, Reserva> reservas = new HashMap<>();

    public Map<Integer, Usuario> usuarios() {
            return usuarios;
    }

    public Map<Integer, Pista> pistas() {
            return pistas;
    }

    public Map<Integer, Reserva> reservas() {
            return reservas;
    }

    //Metodo para el log in
    public Usuario buscarPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    //Para métodos availability y correo programado semanalmente
    public List<String> obtenerDisponibilidadPista(int idPista, LocalDate fecha) {
        List<String> disponibilidad = new ArrayList<>();
        LocalTime hora = LocalTime.of(9, 0); // Hora apertura
        LocalTime cierre = LocalTime.of(22, 0); // Hora cierre

        while (!hora.isAfter(cierre)) {
            LocalTime siguiente = hora.plusMinutes(30);
            final LocalTime horaSlot = hora;
            final LocalTime siguienteSlot = siguiente;

            // Comprobamos si hay alguna reserva que se solape con este slot de 30 min
            boolean ocupada = reservas().values().stream()
                    .anyMatch(r -> r.idPista() == idPista
                            && r.fechaReserva().equals(fecha)
                            && r.horaFin().isAfter(horaSlot)
                            && r.horaInicio().isBefore(siguienteSlot)
                            && r.estado() != Reserva.Estado.CANCELADA);

            // Guardamos la hora y se pone ocupada si lo está
            disponibilidad.add(hora + (ocupada ? " ocupada" : ""));

            hora = siguiente;
        }
        return disponibilidad;
    }

}

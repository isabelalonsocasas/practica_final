package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioReservas {
    @Autowired
    RepoPista repoPista;
    @Autowired
    RepoReserva repoReserva;
    @Autowired
    RepoRol repoRol;
    @Autowired
    RepoUsuario repoUsuario;

    private boolean haySolape(int idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {

        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        for (Reserva reservaExistente : reservas) {

            if (reservaExistente.idPista() != idPista) continue;
            if (!reservaExistente.fechaReserva().equals(fechaReserva)) continue;

            LocalTime horaInicioExistente = reservaExistente.horaInicio();
            LocalTime horaFinExistente = reservaExistente.horaFin();

            boolean solapa = horaInicioExistente.isBefore(horaFinNueva)
                    && horaFinExistente.isAfter(horaInicioNueva);

            if (solapa) return true;
        }

        return false;
    }

    public Reserva crearReserva(ReservaBody reserva, Authentication authentication) {

        Pista pista = repoPista.findById(reserva.idPista()).orElse(null);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        if (haySolape(reserva.idPista(), reserva.fechaReserva(), reserva.horaInicio(), reserva.duracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        String email = authentication.getName();
        Usuario u = repoUsuario.findByEmail(email);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Reserva nueva = new Reserva(
                null,
                u.idUsuario(),
                reserva.idPista(),
                reserva.fechaReserva(),
                reserva.horaInicio(),
                reserva.duracionMinutos()
        );

        return repoReservas.save(nueva);
    }

    public List<Reserva> misReservas(String from, String to, Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }

        LocalDate desde = null;
        LocalDate hasta = null;

        try {
            if (from != null) desde = LocalDate.parse(from);
            if (to != null) hasta = LocalDate.parse(to);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido (YYYY-MM-DD)");
        }

        final LocalDate desdeFinal = desde;
        final LocalDate hastaFinal = hasta;

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        return reservas.stream()
                .filter(r -> r.idUsuario() == usuarioAutenticado.idUsuario())
                .filter(r -> desdeFinal == null || !r.fechaReserva().isBefore(desdeFinal))
                .filter(r -> hastaFinal == null || !r.fechaReserva().isAfter(hastaFinal))
                .sorted(Comparator.comparing(Reserva::fechaReserva).thenComparing(Reserva::horaInicio))
                .toList();
    }

    public Reserva obtenerReserva(int reservationId, Authentication authentication) {

        Reserva actual = repoReservas.findById(reservationId).orElse(null);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) && (actual.idUsuario() == usuarioAutenticado.idUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return actual;
    }

    public void cancelarReserva(int reservationId, Authentication authentication) {

        Reserva actual = repoReservas.findById(reservationId).orElse(null);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) && (actual.idUsuario() == usuarioAutenticado.idUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        if (actual.estado() == Reserva.Estado.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está cancelada");
        }

        if (yaHaEmpezado(actual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar por política");
        }

        Reserva cancelada = new Reserva(
                actual.idReserva(),
                actual.idUsuario(),
                actual.idPista(),
                actual.fechaReserva(),
                actual.horaInicio(),
                actual.duracionMinutos(),
                actual.horaFin(),
                Reserva.Estado.CANCELADA,
                actual.fechaCreacion()
        );

        repoReservas.save(cancelada);
    }

    private boolean yaHaEmpezado(Reserva r) {
        LocalDateTime inicio = LocalDateTime.of(r.fechaReserva(), r.horaInicio());
        return inicio.isBefore(LocalDateTime.now());
    }

    public ResponseEntity<Reserva> modificarReserva(int idReserva, ReservaBody reservaCambio, Authentication authentication) {

        Reserva reserva = repoReservas.findById(idReserva).orElse(null);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) && (reserva.idUsuario() == usuarioAutenticado.idUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        Pista pista = repoPista.findById(reservaCambio.idPista()).orElse(null);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        if (haySolapeModificacion(idReserva,
                reservaCambio.idPista(),
                reservaCambio.fechaReserva(),
                reservaCambio.horaInicio(),
                reservaCambio.duracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        Reserva reservaCambiada = new Reserva(
                idReserva,
                usuarioAutenticado.idUsuario(),
                reservaCambio.idPista(),
                reservaCambio.fechaReserva(),
                reservaCambio.horaInicio(),
                reservaCambio.duracionMinutos()
        );

        repoReservas.save(reservaCambiada);

        return ResponseEntity.ok(reservaCambiada);
    }

    private boolean haySolapeModificacion(int idReserva, int idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {

        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        for (Reserva reservaExistente : reservas) {

            if (reservaExistente.idReserva() == idReserva) continue;
            if (reservaExistente.idPista() != idPista) continue;
            if (!reservaExistente.fechaReserva().equals(fechaReserva)) continue;

            LocalTime horaInicioExistente = reservaExistente.horaInicio();
            LocalTime horaFinExistente = reservaExistente.horaFin();

            boolean solapa = horaInicioExistente.isBefore(horaFinNueva)
                    && horaFinExistente.isAfter(horaInicioNueva);

            if (solapa) return true;
        }

        return false;
    }

    public ResponseEntity<List<Reserva>> getReservas(LocalDate fecha, Integer pista, Integer user) {

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        List<Reserva> reservasFiltro = reservas.stream()
                .filter(r -> fecha == null || r.fechaReserva().equals(fecha))
                .filter(r -> pista == null || r.idPista() == pista)
                .filter(r -> user == null || r.idUsuario() == user)
                .toList();

        return ResponseEntity.ok(reservasFiltro);
    }
}

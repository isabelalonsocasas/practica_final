package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioReservas {
    @Autowired
    RepoPista repoPista;
    @Autowired
    RepoReserva repoReserva;
    @Autowired
    RepoUsuario repoUsuario;

    private boolean haySolape(long idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {
        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        List<Reserva> reservasDelDia = repoReserva.findByPista_IdPistaAndFechaReserva(idPista, fechaReserva);

        for (Reserva reservaExistente : reservasDelDia) {
            if (reservaExistente.getEstado() == Reserva.Estado.CANCELADA) continue;

            LocalTime horaInicioExistente = reservaExistente.getHoraInicio();
            LocalTime horaFinExistente = reservaExistente.getHoraFin();

            boolean solapa = horaInicioExistente.isBefore(horaFinNueva) && horaFinExistente.isAfter(horaInicioNueva);
            if (solapa) return true;
        }
        return false;
    }

    public Reserva crearReserva(Reserva reserva, Authentication authentication) {

        Pista pista = repoPista.findById(reserva.getPista().getIdPista()).orElse(null);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        if (haySolape(reserva.getPista().getIdPista(), reserva.getFechaReserva(), reserva.getHoraInicio(), reserva.getDuracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        String email = authentication.getName();
        Usuario u = repoUsuario.findByEmail(email);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Reserva nueva = new Reserva();
        nueva.setUsuario(u);
        nueva.setPista(pista);
        nueva.setFechaReserva(reserva.getFechaReserva());
        nueva.setHoraInicio(reserva.getHoraInicio());
        nueva.setDuracionMinutos(reserva.getDuracionMinutos());
        nueva.setHoraFin(reserva.getHoraInicio().plusMinutes(reserva.getDuracionMinutos()));
        nueva.setEstado(Reserva.Estado.ACTIVA);

        return repoReserva.save(nueva);
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

        if (desde != null && hasta != null) {
            return repoReserva.findByUsuarioIdUsuarioAndFechaReservaBetweenOrderByFechaReservaAscHoraInicioAsc(usuarioAutenticado.getIdUsuario(), desde, hasta);
        } else if (desde != null) {
            return repoReserva.findByUsuarioIdUsuarioAndFechaReservaGreaterThanEqualOrderByFechaReservaAscHoraInicioAsc(usuarioAutenticado.getIdUsuario(), desde);
        } else if (hasta != null) {
            return repoReserva.findByUsuarioIdUsuarioAndFechaReservaLessThanEqualOrderByFechaReservaAscHoraInicioAsc(usuarioAutenticado.getIdUsuario(), hasta);
        } else {
            return repoReserva.findByUsuarioIdUsuarioOrderByFechaReservaAscHoraInicioAsc(usuarioAutenticado.getIdUsuario());
        }
    }

    public Reserva obtenerReserva(int reservationId, Authentication authentication) {

        Reserva actual = repoReserva.findById((long)reservationId).orElse(null);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) &&
                actual.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return actual;
    }

    public void cancelarReserva(int reservationId, Authentication authentication) {

        Reserva actual = repoReserva.findById((long)reservationId).orElse(null);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) &&
                actual.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        if (actual.getEstado() == Reserva.Estado.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está cancelada");
        }

        if (yaHaEmpezado(actual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar por política");
        }

        actual.setEstado(Reserva.Estado.CANCELADA);

        repoReserva.save(actual);
    }

    private boolean yaHaEmpezado(Reserva r) {
        LocalDateTime inicio = LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());
        return inicio.isBefore(LocalDateTime.now());
    }

    public ResponseEntity<Reserva> modificarReserva(int idReserva, Reserva reservaCambio, Authentication authentication) {

        Reserva reserva = repoReserva.findById((long)idReserva).orElse(null);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = repoUsuario.findByEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = (usuarioAutenticado != null) &&
                reserva.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario());

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        Pista pista = repoPista.findById(reservaCambio.getPista().getIdPista()).orElse(null);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        if (haySolapeModificacion(idReserva,
                reservaCambio.getPista().getIdPista(),
                reservaCambio.getFechaReserva(),
                reservaCambio.getHoraInicio(),
                reservaCambio.getDuracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        reserva.setPista(pista);
        reserva.setFechaReserva(reservaCambio.getFechaReserva());
        reserva.setHoraInicio(reservaCambio.getHoraInicio());
        reserva.setDuracionMinutos(reservaCambio.getDuracionMinutos());
        reserva.setHoraFin(reservaCambio.getHoraInicio().plusMinutes(reservaCambio.getDuracionMinutos()));

        repoReserva.save(reserva);
        return ResponseEntity.ok(reserva);
    }

    private boolean haySolapeModificacion(long idReserva, long idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {
        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        List<Reserva> reservasDelDia = repoReserva.findByPista_IdPistaAndFechaReserva(idPista, fechaReserva);

        for (Reserva reservaExistente : reservasDelDia) {
            if (reservaExistente.getIdReserva().equals(idReserva)) continue;
            if (reservaExistente.getEstado() == Reserva.Estado.CANCELADA) continue;

            LocalTime horaInicioExistente = reservaExistente.getHoraInicio();
            LocalTime horaFinExistente = reservaExistente.getHoraFin();

            boolean solapa = horaInicioExistente.isBefore(horaFinNueva) && horaFinExistente.isAfter(horaInicioNueva);
            if (solapa) return true;
        }
        return false;
    }

    public ResponseEntity<List<Reserva>> getReservas(LocalDate fecha, Integer pista, Integer user) {

        List<Reserva> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        List<Reserva> reservasFiltro = reservas.stream()
                .filter(r -> fecha == null || r.getFechaReserva().equals(fecha))
                .filter(r -> pista == null || r.getPista().getIdPista().equals((long)pista))
                .filter(r -> user == null || r.getUsuario().getIdUsuario().equals((long)user))
                .toList();

        return ResponseEntity.ok(reservasFiltro);
    }
}

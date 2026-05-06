package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioPistas {
    @Autowired
    RepoPista repoPista;
    @Autowired
    RepoReserva repoReserva;


    public Pista idPistaExiste(Long idPista) {
        Pista pista = repoPista.findById(idPista).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }
        return pista;
    }

    public ResponseEntity<Pista> crearPista(Pista pista) {
        Pista pistaExistente = repoPista.findByNombre(pista.getNombre());

        if (pistaExistente != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de la pista ya existe"
            );
        }

        repoPista.save(pista);
        return ResponseEntity.status(HttpStatus.CREATED).body(pista);
    }

    public List<Pista> listarPistas(Boolean active) {
        if (active == null) {
            return (List<Pista>) repoPista.findAll();
        }
        return repoPista.findByActiva(active);
    }

    public ResponseEntity<Pista> getInfoPista(Long idPista) {
        Pista pista = idPistaExiste(idPista);

        return ResponseEntity.ok(pista);
    }

    public ResponseEntity<Pista> actualizarPista(Long idPista, Pista pistaActualizada) {

        Pista pista = idPistaExiste(idPista);

        if (!pista.getNombre().equalsIgnoreCase(pistaActualizada.getNombre())) {
            // Si ha cambiado, verificamos que no esté cogido por OTRA pista distinta

            if (repoPista.existsByNombreIgnoreCaseAndIdPistaNot(pistaActualizada.getNombre(), idPista)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nombre nuevo de la pista ya está siendo utilizado"
                );
            }
        }

        pista.setNombre(pistaActualizada.getNombre());
        pista.setUbicacion(pistaActualizada.getUbicacion());
        pista.setPrecioHora(pistaActualizada.getPrecioHora());
        pista.setActiva(pistaActualizada.isActiva());

        repoPista.save(pista);

        return ResponseEntity.ok(pista);
    }

    public ResponseEntity<Void> eliminarPista(Long idPista) {
        Pista pista = idPistaExiste(idPista);

        boolean tieneReservasFuturas = repoReserva.existsByPista_IdPistaAndFechaReservaAfter(idPista, LocalDate.now().minusDays(1));
        if (tieneReservasFuturas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar una pista con reservas futuras activas");
        }

        repoPista.delete(pista);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public ResponseEntity<Void> desactivarPista(Long idPista) {

        Pista pista = idPistaExiste(idPista);

        pista.setActiva(false);

        repoPista.save(pista);

        return ResponseEntity.noContent().build();
    }

    public List<Map<String, Object>> consultarDisponibilidad(String date, Long idPista) {

        LocalDate fechaConsulta;

        try {
            fechaConsulta = LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        List<Pista> pistas = new ArrayList<>();

        if (idPista != null) {
            Pista p = repoPista.findById(idPista).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada"));
            pistas.add(p);
        } else {
            repoPista.findAll().forEach(pistas::add);
        }

        return pistas.stream()
                .map(p -> {
                    Map<String, Object> pistaInfo = new HashMap<>();
                    pistaInfo.put("nombre", p.getNombre());
                    pistaInfo.put("disponibilidad", obtenerDisponibilidadPista(p.getIdPista(), fechaConsulta));
                    return pistaInfo;
                })
                .toList();
    }

    public Map<String, Object> consultarDisponibilidadPista(String date, Long courtId) {

        LocalDate fechaConsulta;

        try {
            fechaConsulta = LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        Pista pista = repoPista.findById(courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La pista con id " + courtId + " no existe"
            );
        }

        List<String> disponibilidad = obtenerDisponibilidadPista(courtId, fechaConsulta);

        Map<String, Object> infoPista = new HashMap<>();
        infoPista.put("nombre", pista.getNombre());
        infoPista.put("disponibilidad", disponibilidad);

        return infoPista;
    }

    private List<String> obtenerDisponibilidadPista(Long courtId, LocalDate fechaConsulta) {
        List<Reserva> reservasPista = repoReserva.findByPista_IdPistaAndFechaReserva(courtId, fechaConsulta);
        List<String> disponibilidad = new ArrayList<>();

        // Usamos LocalTime en lugar de String para poder hacer comparaciones lógicas
        List<LocalTime> franjas = List.of(
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
                LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
                LocalTime.of(17, 0), LocalTime.of(18, 0), LocalTime.of(19, 0), LocalTime.of(20, 0),
                LocalTime.of(21, 0)
        );

        for (LocalTime franjaInicio : franjas) {
            LocalTime franjaFin = franjaInicio.plusHours(1); // Asumimos slots de 1 hora

            boolean ocupada = reservasPista.stream()
                    .filter(r -> r.getEstado() != Reserva.Estado.CANCELADA) // Descartamos canceladas
                    .anyMatch(r -> r.getHoraInicio().isBefore(franjaFin) && r.getHoraFin().isAfter(franjaInicio));

            if (!ocupada) {
                disponibilidad.add(franjaInicio.toString());
            }
        }

        return disponibilidad;
    }
}
package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;

import java.time.LocalDate;
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
    @Autowired
    RepoRol repoRol;
    @Autowired
    RepoUsuario repoUsuario;
    

    public ResponseEntity<Pista> crearPista(Pista pista) {
    Pista pistaExistente = repoPista.findByNombre(pista.nombre);

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

    public ResponseEntity<Pista> getInfoPista(Integer idPista) {
        Pista pista = repoPista.findById((long)(idPista)).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        return ResponseEntity.ok(pista);
    }

    public ResponseEntity<Pista> actualizarPista(long idPista, Pista pistaActualizada) {

        Pista pista = repoPista.findById(idPista).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        if (!pista.nombre.equalsIgnoreCase(pistaActualizada.nombre)) {
            // Si ha cambiado, verificamos que no esté cogido por OTRA pista distinta

            if (repoPista.existsByNombreIgnoreCaseAndIdPistaNot(pistaActualizada.nombre, idPista)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El nombre nuevo de la pista ya está siendo utilizado"
                );
            }
        }

        pista.nombre = pistaActualizada.nombre;
        pista.ubicacion = pistaActualizada.ubicacion;
        pista.precioHora = pistaActualizada.precioHora;
        pista.activa = pistaActualizada.activa;

        repoPista.save(pista);

        return ResponseEntity.ok(pista);
    }

    public ResponseEntity<Void> desactivarPista(int idPista) {

        Pista pista = repoPista.findById((long) idPista).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        if (repoReserva.existsByPista_IdPistaAndFechaReservaAfter(idPista, LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede desactivar: hay reservas futuras"
            );
        }


        pista.activa = false;

        repoPista.save(pista);

        return ResponseEntity.noContent().build();
    }

    public List<Map<String, Object>> consultarDisponibilidad(String date, Integer courtId) {

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
        repoPista.findAll().forEach(pistas::add);

        return pistas.stream()
                .filter(p -> courtId == null || p.idPista == (long) courtId)
                .map(p -> {
                    Map<String, Object> pistaInfo = new HashMap<>();
                    pistaInfo.put("nombre", p.nombre);
                    pistaInfo.put("disponibilidad", obtenerDisponibilidadPista(p.idPista, fechaConsulta));
                    return pistaInfo;
                })
                .toList();
    }

    public Map<String, Object> consultarDisponibilidadPista(String date, Integer courtId) {

        LocalDate fechaConsulta;

        try {
            fechaConsulta = LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        Pista pista = repoPista.findById((long)courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La pista con id " + courtId + " no existe"
            );
        }

        List<String> disponibilidad = obtenerDisponibilidadPista((long)courtId, fechaConsulta);

        Map<String, Object> infoPista = new HashMap<>();
        infoPista.put("nombre", pista.nombre);
        infoPista.put("disponibilidad", disponibilidad);

        return infoPista;
    }

    private List<String> obtenerDisponibilidadPista(Long courtId, LocalDate fechaConsulta) {

        List<Reserva> reservasPista = repoReserva.findByPista_IdPistaAndFechaReserva(courtId,fechaConsulta);

        List<String> disponibilidad = new ArrayList<>();

        List<String> franjas = List.of(
                "09:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00",
                "17:00", "18:00", "19:00", "20:00", "21:00"
        );

        for (String franja : franjas) {
            boolean ocupada = reservasPista.stream()
                    .anyMatch(r -> r.horaInicio.toString().equals(franja));

            if (!ocupada) {
                disponibilidad.add(franja);
            }
        }

        return disponibilidad;
    }
}


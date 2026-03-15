package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

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
import org.springframework.stereotype.Service;

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
    Pista pistaExistente = repoPista.findByNombre(pista.nombre());

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
        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);

        return pistas.stream().filter(p -> active == null || p.activa() == active).toList();
    }

    public ResponseEntity<Pista> getInfoPista(Integer courtId) {
        Pista pista = repoPista.findById(courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        return ResponseEntity.ok(pista);
    }

    public ResponseEntity<Pista> actualizarPista(int courtId, Pista datosActualizados) {

        Pista pista = repoPista.findById(courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        boolean nombreDuplicado = false;
        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);

        nombreDuplicado = pistas.stream()
                .anyMatch(p ->
                        p.nombre().equalsIgnoreCase(datosActualizados.nombre())
                                && p.idPista() != courtId
                );

        if (nombreDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre nuevo de la pista ya está siendo utilizado"
            );
        }

        Pista actualizada = new Pista(
                courtId,
                datosActualizados.nombre(),
                datosActualizados.ubicacion(),
                datosActualizados.precioHora(),
                datosActualizados.activa(),
                pista.fechaAlta()
        );

        repoPista.save(actualizada);

        return ResponseEntity.ok(actualizada);
    }

    public ResponseEntity<Void> desactivarPista(int courtId) {

        Pista pista = repoPista.findById(courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        boolean hayReservasFuturas = reservas.stream()
                .anyMatch(r -> r.idPista() == courtId && r.fechaReserva().isAfter(LocalDate.now()));

        if (hayReservasFuturas) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede desactivar: hay reservas futuras"
            );
        }

        Pista desactivada = new Pista(
                pista.idPista(),
                pista.nombre(),
                pista.ubicacion(),
                pista.precioHora(),
                false,
                pista.fechaAlta()
        );

        repoPista.save(desactivada);

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
                .filter(p -> courtId == null || p.idPista() == courtId)
                .map(p -> {
                    Map<String, Object> pistaInfo = new HashMap<>();
                    pistaInfo.put("nombre", p.nombre());
                    pistaInfo.put("disponibilidad", obtenerDisponibilidadPista(p.idPista(), fechaConsulta));
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

        Pista pista = repoPista.findById(courtId).orElse(null);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La pista con id " + courtId + " no existe"
            );
        }

        List<String> disponibilidad = obtenerDisponibilidadPista(courtId, fechaConsulta);

        Map<String, Object> infoPista = new HashMap<>();
        infoPista.put("nombre", pista.nombre());
        infoPista.put("disponibilidad", disponibilidad);

        return infoPista;
    }

    private List<String> obtenerDisponibilidadPista(Integer courtId, LocalDate fechaConsulta) {

        List<Reserva> reservas = new ArrayList<>();
        repoReservas.findAll().forEach(reservas::add);

        List<Reserva> reservasPista = reservas.stream()
                .filter(r -> r.idPista() == courtId && r.fechaReserva().equals(fechaConsulta))
                .toList();

        List<String> disponibilidad = new ArrayList<>();

        List<String> franjas = List.of(
                "09:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00",
                "17:00", "18:00", "19:00", "20:00", "21:00"
        );

        for (String franja : franjas) {
            boolean ocupada = reservasPista.stream()
                    .anyMatch(r -> r.horaInicio().toString().equals(franja));

            if (!ocupada) {
                disponibilidad.add(franja);
            }
        }

        return disponibilidad;
    }
}


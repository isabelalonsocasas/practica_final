package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class PadelControlador {

    //IMPORTAMOS NUESTRO ALMACEN DE DATOS
    private final AlmacenDatos almacen;

    public PadelControlador(AlmacenDatos almacen) {
        this.almacen = almacen;
    }


    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();
    private final Map<Integer, Reserva> reservas = new HashMap<>();
    private final Map<String, Usuario> sesiones = new HashMap<>();

    ///  Métodos auth usuario

    //Registrarse (completado)
    @PostMapping("/pistaPadel/auth/register")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        boolean emailExiste = almacen.usuarios().values().stream()
                .anyMatch(u -> u.email().equals(NuevoUsuario.email()));

        if (emailExiste) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya existe"
            );
        }
        almacen.usuarios().put(NuevoUsuario.idUsuario(), NuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(NuevoUsuario); //Refleja 201 created y el usuario

    }

    //LOG IN EN CONFIGURACIÓN DE SEGURIDAD
    // Cambiar a auth. Quitar ResponseEntity<Usuario>
    //@PostMapping("/pistaPadel/auth/login")
    //public ResponseEntity<Usuario> loginUsuario(@Valid @RequestBody Map<String, String> body){ //ResponseEntity para las respuestas. El @Valid da el 400 Bad Request

        // El sistema de login solo recibe el email y la contraseña
        //String email = body.get("email");
        //String password = body.get("password");

        //for (Usuario u :usuarios.values()){
            //if (u.getEmail().equals(email) && u.getPassword().equals(password)){
                //return ResponseEntity.ok(u); // 200 OK, devuelve el usuario 'u' en el body de la respuesta
            //}
        //}
        //throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"); // Lanzamos la excepcion para ser caputurada por el controlador Global de Errores
    //}

    //LOG OUT EN CONFIGURACIÓN DE SEGURIDAD
    //@PostMapping("/pistaPadel/auth/logout")
    //public void logoutUsuario(@Valid @RequestBody Map<String, String> body){
    //}

    //GET USUARIO AUTENTICADO (completado)
    @GetMapping("/pistaPadel/auth/me")
    public ResponseEntity<Usuario> usuarioAutenticado(Authentication authentication) {

        String email = authentication.getName(); // username = email

        Usuario u = almacen.buscarPorEmail(email);

        return ResponseEntity.ok(u);
    }

    /// Métodos users
    // Get users(completado)
    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")// Comprobar autorización de ADMIN
    public Map<Integer, Usuario> getUsuarios(){
        return almacen.usuarios();
    }

    ///  Métodos courts
    // Falta añadir la autorización de admin
    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public Pista crearPista(@Valid @RequestBody Pista pista){
        pistas.put(pista.idPista(), pista);
        return pista;
    }

    @GetMapping("/pistaPadel/courts")
    public List<Pista> listarPistas(){
        return pistas.values()
                .stream() // Permite el procesamiento de datos
                .filter(p -> p.isActive()) // Filtro para comprobar que está activa
                .toList(); // Para poder devolver el JSON
    }

    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista getInfoPista(@PathVariable Integer courtId){
        Pista pista = pistas.get(courtId);

        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pista no encontrada"
            );
        }
        return pista;

    }


    /// Métodos availability

    @GetMapping("/pistaPadel/availability")
    public List<Map<String, Object>> consultarDisponibilidad(@RequestParam String date, @RequestParam(required = false) Integer courtId) {
        LocalDate fechaConsulta;
        try {
            fechaConsulta = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        List<Map<String, Object>> resultado = new ArrayList<>();

        // Filtramos las pistas con el id requerido,
        pistas.values().stream()
                .filter(p -> courtId == null || p.getIdPista().equals(courtId))
                .forEach(p -> {

                    // Hacemos una lista con la pista y sus reservas con sus respectivas horas
                    List<Map<String, String>> reservasPista = reservas.values()
                            .stream()
                            .filter(r -> r.getIdPista().equals(p.getIdPista()) // Por cada reserva, mira si la pista coincide
                                    && r.getFecha().equals(fechaConsulta))
                            .map(r -> Map.of(
                                    "horaInicio", r.getHoraInicio().toString(), // Pasamos a String porque es LocalDate y queremos un mapa de String,String
                                    "horaFin", r.getHoraFin().toString() // Así se presentan visualmente mejor las horas de las reservas
                            ))
                            .toList();

                    // Construir objeto de respuesta
                    Map<String, Object> pistaInfo = new HashMap<>();
                    pistaInfo.put("idPista", p.getIdPista());
                    pistaInfo.put("nombre", p.getNombre());
                    pistaInfo.put("reservas", reservasPista);

                    resultado.add(pistaInfo);
                });

        return resultado;
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public List<Map<String, String>> consultarDisponibilidadPista(@RequestParam String date,@PathVariable Integer courtId){
        // Comprobamos fecha igual que en el método anterior
        LocalDate fechaConsulta;
        try {
            fechaConsulta = LocalDate.parse(date.trim()); // Solo fecha
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        // Para lanzar el 404, comprobamos que existe la pista
        Pista pista = pistas.get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La pista con id " + courtId + " no existe"
            );
        }

        List<Map<String, String>> reservasPista = reservas.values()
                .stream()
                .filter(r -> r.getIdPista().equals(courtId)
                        && r.getFecha().equals(fechaConsulta))
                .map(r -> Map.of(
                        "horaInicio", r.getHoraInicio().toString(),
                        "horaFin", r.getHoraFin().toString()
                ))
                .toList();

        return reservasPista;
    }

    //Reservations
    private final AtomicInteger nextReservaId = new AtomicInteger(1);
    private boolean haySolape(int idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {

        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        for (Reserva reservaExistente : reservas.values()) {

            if (reservaExistente.idPista() != idPista) continue;
            if (!reservaExistente.fechaReserva().equals(fechaReserva)) continue;

            LocalTime horaInicioExistente = reservaExistente.horaInicio();
            LocalTime horaFinExistente    = reservaExistente.horaFin(); // ya calculada en Reserva

            // Solape si: inicioExistente < finNuevo  Y  finExistente > inicioNuevo
            boolean solapa = horaInicioExistente.isBefore(horaFinNueva)
                    && horaFinExistente.isAfter(horaInicioNueva);

            if (solapa) return true;
        }

        return false;
    }
    private record ReservaBody(
            @NotNull @Positive Integer idPista,
            @NotNull LocalDate fechaReserva,
            @NotNull LocalTime horaInicio,
            @NotNull @Positive Integer duracionMinutos
    ) {}


    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crearReserva(@Valid @RequestBody ReservaBody reserva){
        // 404: la pista no existe
        if (!pistas.containsKey(reserva.idPista())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        // 409: slot ocupado (mismo courtId + misma fecha + solape horario)
        if (haySolape(reserva.idPista(), reserva.fechaReserva(), reserva.horaInicio(), reserva.duracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }
        // Generar idReserva
        int idReserva = nextReservaId.getAndIncrement();

        // Usuario (si no hay auth todavía)
        int idUsuario = 0;

        // Usa tu constructor corto de Reserva (6 params)
        Reserva nueva = new Reserva(
                idReserva,
                idUsuario,
                reserva.idPista(),
                reserva.fechaReserva(),
                reserva.horaInicio(),
                reserva.duracionMinutos()
        );

        reservas.put(nueva.idReserva(), nueva);
        return nueva;
    }


    //ADMIN ver Reservas de todos con filtro Opcional
    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reserva>> getReservas(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer courtId,
            @RequestParam(required = false) Integer userId ){

        List<Reserva> reservas = new ArrayList<>(almacen.reservas().values());

        List<Reserva> reservasFiltro = reservas.stream()
                .filter(r -> date == null || r.fechaReserva().equals(date))
                .filter(r -> courtId == null || r.idPista() == (courtId))
                .filter(r -> userId == null || r.idUsuario() == (userId))
                .toList();

        return ResponseEntity.ok(reservasFiltro);
    }
}


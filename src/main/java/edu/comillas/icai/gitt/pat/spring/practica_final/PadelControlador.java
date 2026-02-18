package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@EnableScheduling
public class PadelControlador {

    //IMPORTAMOS NUESTRO ALMACEN DE DATOS
    private final AlmacenDatos almacen;

    public PadelControlador(AlmacenDatos almacen) {
        this.almacen = almacen;
    }
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

    //Get user si eres admin o si eres el usuario autenticado (completado)
    @GetMapping("/pistaPadel/users/{userId}")
    public ResponseEntity<Usuario> obtenerUsuario( @PathVariable Integer userId, Authentication authentication) {

        Usuario usuario = almacen.usuarios().get(userId); //Buscamos en el almacen el usuario

        if (usuario == null) {
            throw new ResponseStatusException( HttpStatus.NOT_FOUND, "Usuario no existe"); //Si no esta el usuario se lanza error
        }

        // Cogemos las creedenciales del login
        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = almacen.buscarPorEmail(emailAutenticado);

        // Comprobamos si es admin
        boolean esAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Comprobar si es el dueño del id el que se ha autenticado
        boolean esDueno = usuarioAutenticado.idUsuario()==userId;

        // Si no es admin ni dueño se prohibe el acceso error (403)
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return ResponseEntity.ok(usuario);
    }

    //Patch actualizar datos de usuario (completado)
    @PatchMapping("/pistaPadel/users/{userId}")
    public ResponseEntity<Usuario> actualizarUsuario( @PathVariable int userId, @Valid @RequestBody Usuario datosActualizados, Authentication authentication) {

        // Busca en el almacen a ver si se encuentra el usuario
        Usuario usuario = almacen.usuarios().get(userId);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe");
        }

        // Comprueba si tiene persmisos por usuario igual o por administador
        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = almacen.buscarPorEmail(emailAutenticado);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = usuarioAutenticado.idUsuario() == userId;

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // Comprobar email duplicado solo si el usuario es distinto porque si es igual el id en principio tiene el mismo email
        boolean emailDuplicado = almacen.usuarios().values().stream()
                .anyMatch(u ->
                        u.email().equals(datosActualizados.email()) && u.idUsuario() != userId );

        if (emailDuplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe");
        }

        // Crear nuevo usuario
        Usuario actualizado = new Usuario(
                userId,
                datosActualizados.nombre(),
                datosActualizados.apellidos(),
                datosActualizados.email(),
                datosActualizados.password(),
                datosActualizados.telefono(),
                usuario.rol(),
                usuario.fechaRegistro(),
                usuario.activo(),
                usuario.reservas()
        );

        almacen.usuarios().put(userId, actualizado);

        return ResponseEntity.ok(actualizado);
    }



    ///  Métodos courts
    //Crear nueva pista (completado)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pistaPadel/courts")
    public ResponseEntity<Pista> crearPista(@Valid @RequestBody Pista pista) {

        // Comprobar si no existe el nombre de la pista
        boolean nombreDuplicado = almacen.pistas().values().stream()
                .anyMatch(p -> p.nombre().equals(pista.nombre()));

        if (nombreDuplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la pista ya existe");
        }

        // Guardar pista
        almacen.pistas().put(pista.idPista(), pista);

        return ResponseEntity.status(HttpStatus.CREATED).body(pista);
    }

    //Listar las pistas, según parámetro de pista activa o inactiva (completo)
    @GetMapping("/pistaPadel/courts")
    public List<Pista> listarPistas(@RequestParam(required = false) Boolean active){
        return almacen.pistas().values()
                .stream() // Permite el procesamiento de datos
                .filter(p -> active == null || p.activa() == active) // Filtro para comprobar que está activa o no dependiendo del parametro
                .toList(); // Para poder devolver el JSON
    }

    //Obtener información de una pista (completo)
    @GetMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Pista>  getInfoPista(@PathVariable Integer courtId){
        Pista pista = almacen.pistas().get(courtId);

        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }
        return ResponseEntity.ok(pista);

    }

    //Editar una pista (completo)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Pista> actualizarPista( @PathVariable int courtId, @Valid @RequestBody Pista datosActualizados) {

        Pista pista = almacen.pistas().get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        boolean nombreDuplicado = almacen.pistas().values().stream()
                .anyMatch(p ->
                        p.nombre().equalsIgnoreCase(datosActualizados.nombre())
                                && p.idPista() != courtId   // importante para no compararse consigo misma
                );

        if (nombreDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre nuevo de la pista ya está siendo utilizado");
        }

        // Actualizar la nueva pista
        Pista actualizada = new Pista(
                courtId,
                datosActualizados.nombre(),
                datosActualizados.ubicacion(),
                datosActualizados.precioHora(),
                datosActualizados.activa(),
                pista.fechaAlta() //No tiene sentido cambiar la fecha de alta
        );

        almacen.pistas().put(courtId, actualizada);

        return ResponseEntity.ok(actualizada);
    }

    //Desactivar una pista (completo)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Void> desactivarPista(@PathVariable int courtId) {

        Pista pista = almacen.pistas().get(courtId);
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        // Por si hay reservas futuras comprobamos
        boolean hayReservasFuturas = almacen.reservas().values().stream()
                .anyMatch(r -> r.idPista() == courtId && r.fechaReserva().isAfter(LocalDate.now()));

        if (hayReservasFuturas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede desactivar: hay reservas futuras");
        }

        //Actualizamos la pista como desactivada con el false
        Pista desactivada = new Pista(
                pista.idPista(),
                pista.nombre(),
                pista.ubicacion(),
                pista.precioHora(),
                false,
                pista.fechaAlta()
        );

        almacen.pistas().put(courtId, desactivada);

        return ResponseEntity.noContent().build();
    }


    /// Métodos availability
    @GetMapping("/pistaPadel/availability")
    public List<Map<String, Object>> consultarDisponibilidad(
            @RequestParam String date,
            @RequestParam(required = false) Integer courtId) {

        LocalDate fechaConsulta;
        try {
            fechaConsulta = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        // Se usa método de disponibilidad del almacén

        return almacen.pistas().values().stream()
                // Filtramos por ID si el parámetro está presente
                .filter(p -> courtId == null || p.idPista() == courtId)
                .map(p -> {
                    Map<String, Object> pistaInfo = new HashMap<>();
                    pistaInfo.put("nombre", p.nombre());

                    // REUTILIZACIÓN DRY: Llamamos al método centralizado en el almacén
                    pistaInfo.put("disponibilidad", almacen.obtenerDisponibilidadPista(p.idPista(), fechaConsulta));

                    return pistaInfo;
                })
                .toList();
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public Map<String, Object> consultarDisponibilidadPista(@RequestParam String date,@PathVariable Integer courtId){
        // Comprobamos fecha igual que en el método anterior
        LocalDate fechaConsulta;
        Pista pista = almacen.pistas().get(courtId);

        try {
            fechaConsulta = LocalDate.parse(date.trim()); // Solo fecha
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido, debe ser YYYY-MM-DD"
            );
        }

        // Para lanzar el 404, comprobamos que existe la pista
        if (pista == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La pista con id " + courtId + " no existe"
            );
        }

        List<String> disponibilidad = almacen.obtenerDisponibilidadPista(courtId, fechaConsulta);

        Map<String, Object> infoPista = new HashMap<>();
        infoPista.put("nombre", pista.nombre());
        infoPista.put("disponibilidad", disponibilidad);
        return infoPista;
    }

    /// Métodos Reservations
    private final AtomicInteger nextReservaId = new AtomicInteger(1);
    private boolean haySolape(int idPista, LocalDate fechaReserva, LocalTime horaInicioNueva, int duracionMinutosNueva) {

        LocalTime horaFinNueva = horaInicioNueva.plusMinutes(duracionMinutosNueva);

        for (Reserva reservaExistente : almacen.reservas().values()) {

            if (reservaExistente.idPista() != idPista) continue;
            if (!reservaExistente.fechaReserva().equals(fechaReserva)) continue;

            LocalTime horaInicioExistente = reservaExistente.horaInicio();
            LocalTime horaFinExistente = reservaExistente.horaFin(); // ya calculada en Reserva

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
    public Reserva crearReserva(@Valid @RequestBody ReservaBody reserva, Authentication authentication) {

        // 404: la pista no existe
        if (!almacen.pistas().containsKey(reserva.idPista())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        // 409: slot ocupado (mismo courtId + misma fecha + solape horario)
        if (haySolape(reserva.idPista(), reserva.fechaReserva(), reserva.horaInicio(), reserva.duracionMinutos())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }
        // Generar idReserva
        int idReserva = nextReservaId.getAndIncrement();

        // Usuario (si no hay auth todavía)
        String email = authentication.getName();
        Usuario u = almacen.buscarPorEmail(email);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        int idUsuario = u.idUsuario();

        Reserva nueva = new Reserva(
                idReserva,
                idUsuario,
                reserva.idPista(),
                reserva.fechaReserva(),
                reserva.horaInicio(),
                reserva.duracionMinutos()
        );

        almacen.reservas().put(nueva.idReserva(), nueva);
        return nueva;
    }

    private boolean yaHaEmpezado(Reserva r) {
        LocalDateTime inicio = LocalDateTime.of(r.fechaReserva(), r.horaInicio());
        return inicio.isBefore(LocalDateTime.now());
    }


    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> misReservas(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        // 401 si no hay login (en tu config ya lo exige, pero por seguridad)
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        // usuario autenticado
        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = almacen.buscarPorEmail(emailAutenticado);

        if (usuarioAutenticado == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        }

        // Parseo de fechas opcionales (from/to)
        LocalDate desde = null;
        LocalDate hasta = null;

        try {
            if (from != null) desde = LocalDate.parse(from);
            if (to != null)   hasta = LocalDate.parse(to);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de fecha inválido (YYYY-MM-DD)");
        }
        final LocalDate desdeFinal = desde;
        final LocalDate hastaFinal = hasta;
        // Filtrar reservas del usuario + rango fechas si aplica
        return almacen.reservas().values().stream()
                .filter(r -> r.idUsuario() == usuarioAutenticado.idUsuario())
                .filter(r -> desdeFinal == null || !r.fechaReserva().isBefore(desdeFinal)) // fecha >= desde
                .filter(r -> hastaFinal == null || !r.fechaReserva().isAfter(hastaFinal))  // fecha <= hasta
                .sorted(Comparator.comparing(Reserva::fechaReserva).thenComparing(Reserva::horaInicio))
                .toList();
    }

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarReserva(@PathVariable int reservationId, Authentication authentication) {

        // 404: no existe
        Reserva actual = almacen.reservas().get(reservationId);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        // usuario autenticado (por email)
        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = almacen.buscarPorEmail(emailAutenticado);

        // ¿admin?
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // ¿dueño?
        boolean esDueno = (usuarioAutenticado != null) && (actual.idUsuario() == usuarioAutenticado.idUsuario());

        // 403: si no es admin ni dueño
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // 409: ya cancelada
        if (actual.estado() == Reserva.Estado.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva ya está cancelada");
        }

        // 409: política mínima (no cancelar si ya ha empezado)
        if (yaHaEmpezado(actual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cancelar por política");
        }

        // Cancelar: record inmutable -> crear copia con estado CANCELADA
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

        almacen.reservas().put(reservationId, cancelada);
    }

    @PatchMapping("/pistaPadel/reservations/{idReserva}")
    public ResponseEntity<Reserva> modificarReserva(
            @PathVariable int idReserva,
            @RequestBody @Valid ReservaBody reservaCambio,
            Authentication authentication
    ) {
        Reserva reserva = almacen.reservas().get(idReserva);
        if (reserva == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no existe");
        }

        // usuario autenticado (por email)
        String emailAutenticado = authentication.getName();
        Usuario usuarioAutenticado = almacen.buscarPorEmail(emailAutenticado);

        // Comprobar si admin
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Comprobar si dueño
        boolean esDueno = (usuarioAutenticado != null) && (reserva.idUsuario() == usuarioAutenticado.idUsuario());

        // 403: si no es admin ni dueño
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // 404: la nueva pista no existe
        if (!almacen.pistas().containsKey(reservaCambio.idPista())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        // 409: slot ocupado (mismo courtId + misma fecha + solape horario)
        if (haySolape(reservaCambio.idPista(), reservaCambio.fechaReserva(), reservaCambio.horaInicio(), reservaCambio.duracionMinutos())) {
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

        almacen.reservas().put(idReserva, reservaCambiada);

        return ResponseEntity.ok(reservaCambiada);
    }

    ///  Método admin

    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reserva>> getReservas(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) Integer pista,
            @RequestParam(required = false) Integer user) {

        List<Reserva> reservas = new ArrayList<>(almacen.reservas().values());

        List<Reserva> reservasFiltro = reservas.stream()
                .filter(r -> fecha == null || r.fechaReserva().toString().equals(fecha))
                .filter(r -> pista == null || r.idPista() == pista)
                .filter(r -> user == null || r.idUsuario() == user)
                .toList();

        return ResponseEntity.ok(reservasFiltro);
    }
}


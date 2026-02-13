package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PadelControlador {
    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();
    private final Map<Integer, Reserva> reservas = new HashMap<>();
    private final Map<String, Usuario> sesiones = new HashMap<>();

    ///  Métodos auth usuario
    @PostMapping("/pistaPadel/auth/register")
    public Usuario registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        usuarios.put(NuevoUsuario.idUsuario(), NuevoUsuario);
        return NuevoUsuario;
    }

    // Cambiar a auth. Quitar ResponseEntity<Usuario>
    @PostMapping("/pistaPadel/auth/login")
    public ResponseEntity<Usuario> loginUsuario(@Valid @RequestBody Map<String, String> body){ //ResponseEntity para las respuestas. El @Valid da el 400 Bad Request

        // El sistema de login solo recibe el email y la contraseña
        String email = body.get("email");
        String password = body.get("password");

        for (Usuario u :usuarios.values()){
            if (u.getEmail().equals(email) && u.getPassword().equals(password)){
                return ResponseEntity.ok(u); // 200 OK, devuelve el usuario 'u' en el body de la respuesta
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"); // Lanzamos la excepcion para ser caputurada por el controlador Global de Errores
    }

    @PostMapping("/pistaPadel/auth/logout")
    public void logoutUsuario(@Valid @RequestBody Map<String, String> body){
    }


    /// Métodos users
    @GetMapping("/pistaPadel/users") // Comprobar autorización de ADMIN
    public Map<Integer, Usuario> getUsuarios(@RequestHeader("Authorization") String token){
        //FALTA IF DE AUTORIZACIÓN
        return usuarios;
    }

    ///  Métodos courts
    // Falta añadir la autorización de admin
    @PostMapping("/pistaPadel/courts")
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
                            .map(r -> Map.<String,String>of(
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
}

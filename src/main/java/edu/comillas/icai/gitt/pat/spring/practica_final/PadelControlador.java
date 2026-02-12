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

import java.util.HashMap;
import java.util.Map;

@RestController
public class PadelControlador {
    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();
    private final Map<Integer, Reserva> reservas = new HashMap<>();

    @PostMapping("/pistaPadel/auth/register")
    public Usuario registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        usuarios.put(NuevoUsuario.idUsuario(), NuevoUsuario);
        return NuevoUsuario;
    }

    @PostMapping("/pistaPadel/auth/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody Usuario usuario){ //ResponseEntity para las respuestas. El @Valid da el 400 Bad Request

        String email = usuario.getEmail();
        String password = usuario.getPassword();

        for (Usuario u :usuarios.values()){
            if (u.getEmail().equals(email) && u.getPassword().equals(password)){
                return ResponseEntity.ok(u); // 200 OK, devuelve el usuario 'u' en el body de la respuesta
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas"); // 401. El .status permite devolver un código HTTP, una explicación completa del error.
    }

}

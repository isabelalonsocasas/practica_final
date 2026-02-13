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

    ///  Métodos auth usuario
    @PostMapping("/pistaPadel/auth/register")
    public Usuario registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        usuarios.put(NuevoUsuario.idUsuario(), NuevoUsuario);
        return NuevoUsuario;
    }

    // Cambiar a auth
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
    public Map<Integer, Usuario> getUsuarios(){
        //FALTA IF DE AUTORIZACIÓN
        return usuarios;
    }

}

package edu.comillas.icai.gitt.pat.spring.practica_final.controlador;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioPistas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioReservas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class UsuarioControlador {
    @Autowired
    ServicioPistas servicioPistas;
    @Autowired
    ServicioUsuarios servicioUsuarios;
    @Autowired
    ServicioReservas servicioReservas;
    @Autowired
    private RepoUsuario repoUsuario;

    ///  Métodos auth usuario

    //Registrarse
    @PostMapping("/pistaPadel/auth/register")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        return servicioUsuarios.registrarUsuario(NuevoUsuario);
    }

    //Get usuario autenticado (completado)
    @GetMapping("/pistaPadel/auth/me")
    public ResponseEntity<edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario> usuarioAutenticado(Authentication authentication) {

        String email = authentication.getName(); // username = email

        edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario u = repoUsuario.findByEmail(email);

        return ResponseEntity.ok(u);
    }
}

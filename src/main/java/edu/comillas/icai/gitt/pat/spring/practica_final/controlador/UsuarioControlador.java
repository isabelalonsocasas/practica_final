package edu.comillas.icai.gitt.pat.spring.practica_final.controlador;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioPistas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioReservas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    ///  Métodos auth usuario

    //Registrarse (completado)
    @PostMapping("/pistaPadel/auth/register")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        return servicioUsuarios.registrarUsuario(NuevoUsuario);
    }
}

package edu.comillas.icai.gitt.pat.spring.practica_final.controlador;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class UsuarioControlador {
    @Autowired
    ServicioUsuarios servicioUsuarios;

    ///  Métodos auth usuario
    //Registrarse
    @PostMapping("/pistaPadel/auth/register")
    public ResponseEntity<Usuario> registrarUsuario(@Valid @RequestBody Usuario NuevoUsuario) {
        return servicioUsuarios.registrarUsuario(NuevoUsuario);
    }

    //Get usuario autenticado (completado)
    @GetMapping("/pistaPadel/auth/me")
    public ResponseEntity<Usuario> usuarioAutenticado(Authentication authentication) {
        Usuario usuario = servicioUsuarios.getUsuarioLogueado(authentication);
        return ResponseEntity.ok(usuario);
    }

    //Get Users
    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")// Comprobar autorización de ADMIN
    public List<Usuario> getUsuarios(@RequestParam(required = false) Boolean activo){
        return servicioUsuarios.getUsuarios(activo);
    }

    //Get user si eres admin o si eres el usuario autenticado (completado)
    @GetMapping("/pistaPadel/users/{userId}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long userId, Authentication authentication) {
        return servicioUsuarios.getUsuario(userId, authentication);
    }

    //Patch actualizar datos de usuario (completado)
    @PatchMapping("/pistaPadel/users/{userId}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long userId, @Valid @RequestBody Usuario usuarioActualizado, Authentication authentication) {
        return servicioUsuarios.actualizaUsuario(userId, usuarioActualizado, authentication);
    }
}

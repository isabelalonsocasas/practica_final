package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServicioUsuarios {
    @Autowired
    RepoPista repoPista;
    @Autowired
    RepoReserva repoReserva;
    @Autowired
    RepoRol repoRol;
    @Autowired
    RepoUsuario repoUsuario;

    ///  Métodos usuario

    //Registrarse (completado)
    public ResponseEntity<Usuario> registrarUsuario(Usuario NuevoUsuario) {
        Usuario usuario = repoUsuario.findByEmail(NuevoUsuario.email);

        if (usuario != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya existe"
            );
        }
        repoUsuario.save(NuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(NuevoUsuario); //Refleja 201 created y el usuario
    }

    //Logueado
    public Usuario getUsuarioLogueado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Usuario usuario = repoUsuario.findByEmail(authentication.getName());
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado en BD");
        }
        return usuario;
    }

    public List<Usuario> getUsuarios(Boolean activo) {
        if (activo == null) {
            return (List<Usuario>) repoUsuario.findAll();
        }
        return repoUsuario.findByActivo(activo);
    }

    public ResponseEntity<Usuario> getUsuario(Long userId, Authentication authentication) {
        Usuario usuarioLogueado = getUsuarioLogueado(authentication);

        // Comprobamos si es admin
        boolean esAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Comprobar si es el dueño del id el que se ha autenticado
        boolean esDueno = usuarioLogueado.idUsuario==userId;

        // Si no es admin ni dueño se prohibe el acceso error (403)
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return ResponseEntity.status(HttpStatus.OK).body(usuarioLogueado);
    }
}

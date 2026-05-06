package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServicioUsuarios {
    @Autowired
    RepoRol repoRol;
    @Autowired
    RepoUsuario repoUsuario;

    ///  Métodos usuario

    //Registrarse
    public ResponseEntity<Usuario> registrarUsuario(Usuario nuevoUsuario) {
        Usuario usuario = repoUsuario.findByEmail(nuevoUsuario.getEmail());

        if (usuario != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya existe"
            );
        }

        nuevoUsuario.setRol(repoRol.findByNombreRol(Rol.NombreRol.USER));

        repoUsuario.save(nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario); //Refleja 201 created y el usuario
    }

    //Logueado
    public Usuario getUsuarioLogueado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado");
        }

        Usuario usuario = repoUsuario.findByEmail(authentication.getName());
        if (usuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado en BD");
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
        boolean esDueno = usuarioLogueado.getIdUsuario().equals(userId);

        // Si no es admin ni dueño se prohibe el acceso error (403)
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado");
        }

        Usuario usuarioBuscado = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return ResponseEntity.status(HttpStatus.OK).body(usuarioBuscado);
    }

    public ResponseEntity<Usuario> actualizaUsuario(Long userId, Usuario usuarioActualizado, Authentication authentication) {

        Usuario usuarioExistente = repoUsuario.findById(userId).orElse(null);

        if (usuarioExistente == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no existe");
        }

        // Comprueba si tiene persmisos por usuario igual o por administador
        Usuario usuarioLogueado = getUsuarioLogueado(authentication);

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean esDueno = usuarioLogueado.getIdUsuario() == userId;

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado");
        }

        // Comprobar email duplicado: Buscamos si ya hay alguien en la BD con ese email
        Usuario usuarioMismoEmail = repoUsuario.findByEmail(usuarioActualizado.getEmail());

        // Si encontramos un usuario con ese email, y su ID NO es el del usuario que estamos editando, es un conflicto
        if (usuarioMismoEmail != null && !usuarioMismoEmail.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está en uso por otra cuenta");
        }

        // Modificar campos
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellidos(usuarioActualizado.getApellidos());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setPassword(usuarioActualizado.getPassword());
        usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
        usuarioExistente.setActivo(usuarioActualizado.getActivo());

        if (esAdmin) {
            usuarioExistente.setRol(usuarioActualizado.getRol());
            usuarioExistente.setFechaRegistro(usuarioActualizado.getFechaRegistro());
        }

        repoUsuario.save(usuarioExistente);

        return ResponseEntity.status(HttpStatus.OK).body(usuarioExistente);
    }
}




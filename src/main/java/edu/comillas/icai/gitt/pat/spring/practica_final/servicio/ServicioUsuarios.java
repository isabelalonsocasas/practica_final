package edu.comillas.icai.gitt.pat.spring.practica_final.servicio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
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
    RepoPista repoPista;
    @Autowired
    RepoReserva repoReserva;
    @Autowired
    RepoRol repoRol;
    @Autowired
    RepoUsuario repoUsuario;

    ///  Métodos usuario

    //Registrarse (completado)
    public ResponseEntity<Usuario> registrarUsuario(Usuario nuevoUsuario) {
        Usuario usuario = repoUsuario.findByEmail(nuevoUsuario.email);

        if (usuario != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya existe"
            );
        }

        if (nuevoUsuario.rol != null && nuevoUsuario.rol.idRol != null) {
            Rol rol = repoRol.findById(nuevoUsuario.rol.idRol).orElse(null);

            if (rol == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El ID de rol proporcionado no existe"
                );
            }
            // Sustituimos el rol incompleto por el rol lleno de datos
            nuevoUsuario.rol = rol;
        }

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
        boolean esDueno = usuarioLogueado.idUsuario==userId;

        // Si no es admin ni dueño se prohibe el acceso error (403)
        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado");
        }

        return ResponseEntity.status(HttpStatus.OK).body(usuarioLogueado);
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

        boolean esDueno = usuarioLogueado.idUsuario == userId;

        if (!esAdmin && !esDueno) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No autorizado");
        }

        // Comprobar email duplicado: Buscamos si ya hay alguien en la BD con ese email
        Usuario usuarioMismoEmail = repoUsuario.findByEmail(usuarioActualizado.email);

        // Si encontramos un usuario con ese email, y su ID NO es el del usuario que estamos editando, es un conflicto
        if (usuarioMismoEmail != null && !usuarioMismoEmail.idUsuario.equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está en uso por otra cuenta");
        }

        // Modificar campos
        usuarioExistente.nombre = usuarioActualizado.nombre;
        usuarioExistente.apellidos = usuarioActualizado.apellidos;
        usuarioExistente.email = usuarioActualizado.email;
        usuarioExistente.password = usuarioActualizado.password;
        usuarioExistente.telefono = usuarioActualizado.telefono;
        usuarioExistente.rol = usuarioActualizado.rol;
        usuarioExistente.fechaRegistro = usuarioActualizado.fechaRegistro;
        usuarioExistente.activo = usuarioActualizado.activo;

        repoUsuario.save(usuarioExistente);

        return ResponseEntity.status(HttpStatus.OK).body(usuarioExistente);
    }
}




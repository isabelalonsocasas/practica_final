package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;

import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioUsuariosTest {

    @Mock
    private RepoUsuario repoUsuario;

    @InjectMocks
    private ServicioUsuarios servicioUsuarios;

    @Test
    void registrarUsuarioCorrecto() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.idUsuario = 1L;
        nuevoUsuario.nombre = "Juan";
        nuevoUsuario.apellidos = "Perez";
        nuevoUsuario.email = "juan@mail.com";
        nuevoUsuario.password = "1234";
        nuevoUsuario.telefono = "666666666";
        nuevoUsuario.rol = new Rol();
        nuevoUsuario.activo = true;

        when(repoUsuario.findByEmail("juan@mail.com")).thenReturn(null);

        ResponseEntity<Usuario> respuesta = servicioUsuarios.registrarUsuario(nuevoUsuario);

        assertEquals(201, respuesta.getStatusCode().value());
        assertEquals(nuevoUsuario, respuesta.getBody());
    }

    @Test
    void registrarUsuarioEmailDuplicado() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.idUsuario = 1L;
        nuevoUsuario.nombre = "Juan";
        nuevoUsuario.apellidos = "Perez";
        nuevoUsuario.email = "juan@mail.com";
        nuevoUsuario.password = "1234";
        nuevoUsuario.telefono = "666666666";
        nuevoUsuario.rol = new Rol();
        nuevoUsuario.activo = true;

        when(repoUsuario.findByEmail("juan@mail.com")).thenReturn(nuevoUsuario);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicioUsuarios.registrarUsuario(nuevoUsuario)
        );

        assertEquals(409, ex.getStatusCode().value());
    }
}


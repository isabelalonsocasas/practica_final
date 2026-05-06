package edu.comillas.icai.gitt.pat.spring.practica_final.test;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
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

    @Mock //Simular objetos
    private RepoUsuario repoUsuario;

    @Mock
    private RepoRol repoRol;

    @InjectMocks //Clase que queremos testear
    private ServicioUsuarios servicioUsuarios;

    @Test
    void registrarUsuarioCorrecto() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setIdUsuario(1L);
        nuevoUsuario.setNombre("Juan");
        nuevoUsuario.setApellidos("Perez");
        nuevoUsuario.setEmail("juan@mail.com");
        nuevoUsuario.setPassword("1234");
        nuevoUsuario.setTelefono("666666666");
        nuevoUsuario.setActivo(true);

        Rol rolUser = new Rol(Rol.NombreRol.USER, "Usuario estándar");
        //Simula mail no existe
        when(repoUsuario.findByEmail("juan@mail.com")).thenReturn(null);

        //Búsqueda del ROL usuario simulada
        when(repoRol.findByNombreRol(Rol.NombreRol.USER)).thenReturn(rolUser);

        ResponseEntity<Usuario> respuesta = servicioUsuarios.registrarUsuario(nuevoUsuario);

        assertEquals(201, respuesta.getStatusCode().value());
        assertEquals(nuevoUsuario, respuesta.getBody());
        assertEquals(rolUser, respuesta.getBody().getRol());
    }

    @Test
    void registrarUsuarioEmailDuplicado() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setIdUsuario(1L);
        nuevoUsuario.setNombre("Juan");
        nuevoUsuario.setApellidos("Perez");
        nuevoUsuario.setEmail("juan@mail.com");
        nuevoUsuario.setPassword("1234");
        nuevoUsuario.setTelefono("666666666");
        nuevoUsuario.setRol(new Rol());
        nuevoUsuario.setActivo(true);

        when(repoUsuario.findByEmail("juan@mail.com")).thenReturn(nuevoUsuario);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicioUsuarios.registrarUsuario(nuevoUsuario)
        );

        assertEquals(409, ex.getStatusCode().value());
    }
}


package edu.comillas.icai.gitt.pat.spring.practica_final.test;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;


import static org.junit.jupiter.api.Assertions.*;

// Para integración y E2E utilizamos RestTemplate y no mocl porque no queremos simular nada,
// queremos comprobar la conexión real entre capas.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UsuarioIntegracionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepoUsuario repoUsuario;

    @Autowired
    private RepoRol repoRol;

    @BeforeEach
    void prepararBD() {
        repoUsuario.deleteAll();
        repoRol.deleteAll();
        repoRol.save(new Rol(Rol.NombreRol.USER, "Usuario estándar"));
    }

    @Test
    void registrarUsuarioCorrecto_devuelve201() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellidos("Perez");
        usuario.setEmail("juan@mail.com");
        usuario.setPassword("1234");
        usuario.setTelefono("666666666");
        usuario.setActivo(true);

        ResponseEntity<Usuario> respuesta = restTemplate.postForEntity(
                "/pistaPadel/auth/register",
                usuario,
                Usuario.class
        );

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("juan@mail.com", respuesta.getBody().getEmail());
        assertEquals(Rol.NombreRol.USER, respuesta.getBody().getRol().getNombreRol());
    }

    @Test
    void registrarUsuarioEmailDuplicado_devuelve409() {
        Rol rolUser = repoRol.findByNombreRol(Rol.NombreRol.USER);

        Usuario existente = new Usuario();
        existente.setNombre("Juan");
        existente.setApellidos("Perez");
        existente.setEmail("juan@mail.com");
        existente.setPassword("1234");
        existente.setTelefono("666666666");
        existente.setActivo(true);
        existente.setRol(rolUser);

        repoUsuario.save(existente);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Juan");
        nuevoUsuario.setApellidos("Perez");
        nuevoUsuario.setEmail("juan@mail.com");
        nuevoUsuario.setPassword("1234");
        nuevoUsuario.setTelefono("666666666");
        nuevoUsuario.setActivo(true);

        ResponseEntity<String> respuesta = restTemplate.postForEntity(
                "/pistaPadel/auth/register",
                nuevoUsuario,
                String.class
        );

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
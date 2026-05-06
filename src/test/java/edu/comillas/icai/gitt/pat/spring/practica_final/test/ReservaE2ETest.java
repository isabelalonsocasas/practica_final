package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoRol;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ReservaE2ETest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private RepoPista repoPista;
	@Autowired
	private RepoUsuario repoUsuario;
	@Autowired
	private RepoReserva repoReserva;
	@Autowired
	private RepoRol repoRol;

	@LocalServerPort
	private int port;

	private String getBaseUrl() {
		return "http://localhost:" + port + "/pistaPadel/courts";
	}

	@Test
	public void crearYobtenerReserva() throws Exception {
		// Se vacían los repositorios
		repoReserva.deleteAll();
		repoPista.deleteAll();
		repoUsuario.deleteAll();
		repoRol.deleteAll();

		// Se crea un rol para el usuario
		Rol rol = repoRol.save(new Rol(Rol.NombreRol.USER, "Usuario normal"));

		// Usuario para el test
		Usuario usuario = new Usuario();
		usuario.setNombre("Gonzalo");
		usuario.setApellidos("García");
		usuario.setEmail("gonzalo@gmail.com");
		usuario.setPassword("1234");
		usuario.setTelefono("699000999");
		usuario.setRol(rol);
		repoUsuario.save(usuario);

		// Pista para el test
		Pista pista = new Pista();
		pista.setNombre("Pista Test");
		pista.setUbicacion("Zona alta sur");
		pista.setPrecioHora(10.0);
		pista.setActiva(true);
		repoPista.save(pista);

		// Se autentica el usuario
		TestRestTemplate authrestTemplate = restTemplate.withBasicAuth("gonzalo@gmail.com", "1234");

		// Se construye el JSON para el POST de Reserva
		String json = "{" +
				"\"pista\": {\"idPista\": " + pista.getIdPista() + "}, " +
				"\"fechaReserva\":\"2026-05-06\"," +
				"\"horaInicio\":\"10:00:00\"," +
				"\"duracionMinutos\": 90" +
				"}";

		// POST
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<String> responseCreate = authrestTemplate.exchange(
				"/pistaPadel/reservations",
				HttpMethod.POST,
				new HttpEntity<>(json, headers),
				String.class);

		// ASSERTS para verificar el POST
		Assertions.assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());
		Assertions.assertNotNull(responseCreate.getBody());
		Assertions.assertFalse(responseCreate.getBody().isBlank());



		// Ahora se hace el GET usando el ID real
		ResponseEntity<String> responseGet = authrestTemplate.exchange(
				"/pistaPadel/reservations/1",
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class);

		// Se verifica el codigo de estado de la respuesta
		Assertions.assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		Assertions.assertNotNull(responseGet.getBody());
		Assertions.assertFalse(responseGet.getBody().isBlank());
		Assertions.assertTrue(responseGet.getBody().contains("2026-05-06"));
	}

//	@Test
//	void listarPistas_sinFiltro_devuelveTodas() {
//		repoPista.deleteAll();
//
//		Pista p1 = new Pista();
//		p1.setNombre("Pista Test 1");
//		p1.setUbicacion("Norte");
//		p1.setPrecioHora(10.0);
//		p1.setActiva(true);
//
//		Pista p2 = new Pista();
//		p2.setNombre("Pista Test 2");
//		p2.setUbicacion("Sur");
//		p2.setPrecioHora(12.0);
//		p2.setActiva(false);
//
//		repoPista.save(p1);
//		repoPista.save(p2);
//
//		ResponseEntity<Pista[]> response = restTemplate.getForEntity(getBaseUrl(), Pista[].class);
//
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//		assertNotNull(response.getBody());
//		assertEquals(2, response.getBody().length);
//	}
//
//	@Test
//	void listarPistas_filtrandoActivas() {
//		// Arrange
//		repoPista.deleteAll();
//
//		Pista activa = new Pista();
//		activa.setNombre("Pista Activa");
//		activa.setUbicacion("Norte");
//		activa.setPrecioHora(10.0);
//		activa.setActiva(true);
//
//		Pista inactiva = new Pista();
//		inactiva.setNombre("Pista Inactiva");
//		inactiva.setUbicacion("Sur");
//		inactiva.setPrecioHora(12.0);
//		inactiva.setActiva(false);
//
//		repoPista.save(activa);
//		repoPista.save(inactiva);
//
//		// Act
//		ResponseEntity<Pista[]> response =
//				restTemplate.getForEntity(
//						getBaseUrl() + "?active=true",
//						Pista[].class
//				);
//
//		// Assert
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//		assertNotNull(response.getBody());
//		assertEquals(1, response.getBody().length);
//		assertTrue(response.getBody()[0].isActiva());
//	}
}
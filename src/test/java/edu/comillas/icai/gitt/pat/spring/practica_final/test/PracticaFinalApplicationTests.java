//package edu.comillas.icai.gitt.pat.spring.practica_final;
//
//import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
//import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.ResponseEntity;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class PracticaFinalApplicationTests {
//
//	@LocalServerPort
//	private int port;
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Autowired
//	private RepoPista repoPista;
//
//	private String getBaseUrl() {
//		return "http://localhost:" + port + "/pistaPadel/courts";
//	}
//
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
//		assertEquals(200, response.getStatusCode().value());
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
//		activa.setActiva(true);
//
//		Pista inactiva = new Pista();
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
//		assertEquals(200, response.getStatusCodeValue());
//		assertNotNull(response.getBody());
//		assertEquals(1, response.getBody().length);
//		assertTrue(response.getBody()[0].getActiva());
//	}
// }
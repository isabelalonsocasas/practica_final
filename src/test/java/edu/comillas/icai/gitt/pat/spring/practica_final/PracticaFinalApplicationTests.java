package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PracticaFinalApplicationTest{

	@Test
	void contextLoads() {
	}

	@Autowired
	private RepoPista pistaRepository;

	@Test
	void listarPistas_conDatos() {
		Pista pista = new Pista();
		pistaRepository.save(pista);

		ResponseEntity<Pista[]> response =
				restTemplate.getForEntity("/pistaPadel/courts?active=true", Pista[].class);

		assertEquals(200, response.getStatusCodeValue());
		assertTrue(response.getBody().length > 0);
	}

}

package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RepoPista extends CrudRepository<Pista, Long> {
    List<Pista> findByActiva(boolean activa);
    Pista findByNombre(String nombre);
    boolean existsByNombreIgnoreCaseAndIdPistaNot(String nombre, long idPista);
}
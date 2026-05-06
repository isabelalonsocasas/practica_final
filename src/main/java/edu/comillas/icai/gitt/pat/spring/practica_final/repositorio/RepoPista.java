package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoPista extends CrudRepository<Pista, Long> {
    List<Pista> findByActiva(Boolean activa);
    Pista findByNombre(String nombre);
    Boolean existsByNombreIgnoreCaseAndIdPistaNot(String nombre, long idPista);
}
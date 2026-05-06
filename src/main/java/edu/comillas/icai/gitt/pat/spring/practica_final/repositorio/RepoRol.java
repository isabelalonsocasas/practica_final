package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Rol;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoRol extends CrudRepository<Rol, Long> {
    Rol findByNombreRol(Rol.NombreRol nombreRol);
}
package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoUsuario extends CrudRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    List<Usuario> findByActivo(Boolean activo);
}
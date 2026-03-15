package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RepoReserva extends CrudRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario); //Reservas por usuario
    List<Reserva> findByPista(Pista pista); //Reservas por pista
    List<Reserva> findByEstado(Reserva.Estado estado); //Reservas por estado

}
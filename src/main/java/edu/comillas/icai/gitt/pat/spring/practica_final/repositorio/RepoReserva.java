package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepoReserva extends CrudRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario); //Reservas por usuario
    List<Reserva> findByPista(Pista pista); //Reservas por pista
    List<Reserva> findByEstado(Reserva.Estado estado);
    List<Reserva> findByPista_IdPistaAndFechaReserva(Long idPista, LocalDate fechaReserva);
    Boolean existsByPista_IdPistaAndFechaReservaAfter(Long idPista, LocalDate fecha);//Reservas por estado
    List<Reserva> findByUsuarioIdUsuarioOrderByFechaReservaAscHoraInicioAsc(Long idUsuario);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaGreaterThanEqualOrderByFechaReservaAscHoraInicioAsc(Long idUsuario, LocalDate desde);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaLessThanEqualOrderByFechaReservaAscHoraInicioAsc(Long idUsuario, LocalDate hasta);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaBetweenOrderByFechaReservaAscHoraInicioAsc(Long idUsuario, LocalDate desde, LocalDate hasta);
    List<Reserva> findByFechaReservaAndEstado(LocalDate fecha, Reserva.Estado estado);

}
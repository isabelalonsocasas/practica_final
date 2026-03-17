package edu.comillas.icai.gitt.pat.spring.practica_final.repositorio;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepoReserva extends CrudRepository<Reserva, Long> {
    List<Reserva> findByUsuario(Usuario usuario); //Reservas por usuario
    List<Reserva> findByPista(Pista pista); //Reservas por pista
    List<Reserva> findByEstado(Reserva.Estado estado);
    List<Reserva> findByPista_IdPistaAndFechaReserva(Long idPista, LocalDate fechaReserva);
    boolean existsByPista_IdPistaAndFechaReservaAfter(long idPista, LocalDate fecha);//Reservas por estado
    List<Reserva> findByUsuarioIdUsuarioOrderByFechaReservaAscHoraInicioAsc(Long idUsuario);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaGreaterThanEqualOrderByFechaReservaAscHoraInicioAsc(long idUsuario, LocalDate desde);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaLessThanEqualOrderByFechaReservaAscHoraInicioAsc(long idUsuario, LocalDate hasta);
    List<Reserva> findByUsuarioIdUsuarioAndFechaReservaBetweenOrderByFechaReservaAscHoraInicioAsc(long idUsuario, LocalDate desde, LocalDate hasta);

}
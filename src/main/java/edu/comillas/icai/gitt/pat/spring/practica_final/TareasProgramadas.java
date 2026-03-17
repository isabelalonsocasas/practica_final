package edu.comillas.icai.gitt.pat.spring.practica_final;


import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TareasProgramadas {

    private Logger logger = LoggerFactory.getLogger(getClass());

    //IMPORTAMOS NUESTRO ALMACEN DE DATOS
    //private final AlmacenDatos almacen;

    //public TareasProgramadas(AlmacenDatos almacen){this.almacen = almacen;}

    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired
    RepoPista repoPista;


    @Scheduled(cron = "0 0 2 * * *")
    public void recordatorioReserva(){
        LocalDate hoy = LocalDate.now();

        List<Reserva> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        for (Reserva r : reservas){

            if (r.fechaReserva.equals(hoy)  && r.estado == Reserva.Estado.ACTIVA) {

                Usuario u = r.usuario;
                System.out.println("=================================");
                System.out.println("EMAIL SIMULADO");
                System.out.println("Para: " + u.email);
                System.out.println("Asunto: Recordatorio Reserva");
                System.out.println("Mensaje: Le recordamos su reserva de hoy día " + hoy + " a las " + r.horaInicio +"h. Dispondrá de " + r.duracionMinutos+ " minutos de uso.");
                System.out.println("=================================");
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * 1")
    public void correoSemanal(){

        logger.info("Generando reporte semanal de disponibilidad...");

        String reporteDisponibilidad = generarCuerpoDisponibilidad();

        List<Usuario> usuarios = new ArrayList<>();
        repoUsuario.findAll().forEach(usuarios::add);

        for (Usuario u : usuarios) {
            System.out.println("=================================");
            System.out.println("EMAIL SEMANAL OPTIMIZADO");
            System.out.println("Para: " + u.email);
            System.out.println("Asunto: Disponibilidad de Pistas para esta semana");
            System.out.println("Hola " + u.nombre + ",");
            System.out.println(reporteDisponibilidad);
            System.out.println("=================================");
        }
    }

    private String generarCuerpoDisponibilidad() {
        StringBuilder sb = new StringBuilder();
        LocalDate hoy = LocalDate.now();
        //Se toman los siguientes 7 días
        List<LocalDate> semana = hoy.datesUntil(hoy.plusDays(7)).toList();

        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);

        List<Reserva> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        sb.append("Aquí tienes el resumen de slots libres para los próximos 7 días:\n");

        for (LocalDate dia : semana) {
            sb.append("\n--- ").append(dia.getDayOfWeek()).append(" (").append(dia).append(") ---\n");

            pistas.stream()
                    .filter(p -> p.activa)
                    .forEach(pista -> {
                        List<String> libres = obtenerDisponibilidadPista(pista.idPista, dia)
                                .stream()
                                .filter(s -> !s.contains("ocupada"))
                                .toList();
                        sb.append("- ").append(pista.nombre).append(": ");
                        if (libres.isEmpty()) {
                            sb.append("Sin disponibilidad.");
                        } else {
                            sb.append(String.join(", ", libres));
                        }
                        sb.append("\n");
                    });
        }
        return sb.toString();
    }



    //PASAMOS OBTENER DISPONIBILIDAD DE ALMACEN AQUI
    public List<String> obtenerDisponibilidadPista(long idPista, LocalDate fecha) {
        List<String> disponibilidad = new ArrayList<>();
        LocalTime hora = LocalTime.of(9, 0); // Hora apertura
        LocalTime cierre = LocalTime.of(22, 0); // Hora cierre

        List<Reserva> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        while (!hora.isAfter(cierre)) {
            LocalTime siguiente = hora.plusMinutes(30);
            final LocalTime horaSlot = hora;
            final LocalTime siguienteSlot = siguiente;

            boolean ocupada = reservas.stream()
                    .anyMatch(r -> r.pista.idPista.equals((long) idPista)
                            && r.fechaReserva.equals(fecha)
                            && r.horaFin.isAfter(horaSlot)
                            && r.horaInicio.isBefore(siguienteSlot)
                            && r.estado != Reserva.Estado.CANCELADA);

            disponibilidad.add(hora + (ocupada ? " ocupada" : ""));

            hora = siguiente;
        }
        return disponibilidad;
    }

//    private List<String> obtenerSlotsLibres(int idPista, LocalDate fecha) {
//        List<String> libres = new ArrayList<>();
//        LocalTime hora = LocalTime.of(9, 0);
//        LocalTime cierre = LocalTime.of(22, 0);
//
//        while (!hora.isAfter(cierre)) {
//            LocalTime finSlot = hora.plusMinutes(30);
//            final LocalTime h = hora;
//
//            // Optimizamos la comprobación de ocupación
//            boolean ocupada = almacen.reservas().values().stream()
//                    .anyMatch(r -> r.idPista() == idPista
//                            && r.fechaReserva().equals(fecha)
//                            && r.horaFin().isAfter(h)
//                            && r.horaInicio().isBefore(finSlot)
//                            && r.estado() != Reserva.Estado.CANCELADA);
//
//            if (!ocupada) {
//                libres.add(hora.toString());
//            }
//            hora = finSlot;
//        }
//        return libres;
//    }

}

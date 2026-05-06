package edu.comillas.icai.gitt.pat.spring.practica_final;


import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Usuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioPistas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TareasProgramadas {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RepoReserva repoReserva;

    @Autowired
    RepoUsuario repoUsuario;

    @Autowired
    RepoPista repoPista;

    @Autowired
    ServicioPistas servicioPistas;


    @Scheduled(cron = "0 0 2 * * *")
    public void recordatorioReserva(){
        LocalDate hoy = LocalDate.now();

        List<Reserva> reservasHoy = repoReserva.findByFechaReservaAndEstado(hoy, Reserva.Estado.ACTIVA);

        for (Reserva r : reservasHoy){
            Usuario u = r.getUsuario();
            System.out.println("=================================");
            System.out.println("EMAIL SIMULADO");
            System.out.println("Para: " + u.getEmail());
            System.out.println("Asunto: Recordatorio Reserva");
            System.out.println("Mensaje: Le recordamos su reserva de hoy día " + hoy + " a las " + r.getHoraInicio() +"h. Dispondrá de " + r.getDuracionMinutos()+ " minutos de uso.");
            System.out.println("=================================");
            }
        }
    }

    @Scheduled(cron = "0 0 9 1 * *")
    public void correoMensual(){

        logger.info("Generando reporte mensual de disponibilidad...");

        String reporteDisponibilidad = generarCuerpoDisponibilidad();

        List<Usuario> usuarios = new ArrayList<>();
        repoUsuario.findAll().forEach(usuarios::add);

        for (Usuario u : usuarios) {
            System.out.println("=================================");
            System.out.println("EMAIL MENSUAL OPTIMIZADO");
            System.out.println("Para: " + u.getEmail());
            System.out.println("Asunto: Disponibilidad de Pistas para este mes");
            System.out.println("Hola " + u.getEmail() + ",");
            System.out.println(reporteDisponibilidad);
            System.out.println("=================================");
        }
    }

    private String generarCuerpoDisponibilidad() {
        StringBuilder sb = new StringBuilder();
        LocalDate hoy = LocalDate.now();
        List<LocalDate> mes = hoy.datesUntil(hoy.plusDays(30)).toList();

        List<Pista> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);

        sb.append("Aquí tienes el resumen de slots libres para los próximos 30 días:\n");

        for (LocalDate dia : mes) {
            sb.append("\n--- ").append(dia.getDayOfWeek()).append(" (").append(dia).append(") ---\n");

            pistas.stream()
                    .filter(p -> p.isActiva())
                    .forEach(pista -> {
                        Map<String, Object> info = servicioPistas.consultarDisponibilidadPista(dia.toString(), pista.getIdPista());
                        List<String> libres = (List<String>) info.get("disponibilidad");
                        sb.append("- ").append(pista.getNombre()).append(": ");
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
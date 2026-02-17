package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

public class TareasProgramadas {

    //IMPORTAMOS NUESTRO ALMACEN DE DATOS
    private final AlmacenDatos almacen;

    public TareasProgramadas(AlmacenDatos almacen){this.almacen = almacen;}

    @Scheduled(cron = "0 0 2 * * *")
    public void recordatorioReserva(){
        LocalDate hoy = LocalDate.now();

        for (Reserva r : almacen.reservas().values()){

            if (r.fechaReserva().equals(hoy)) {

                Usuario u = almacen.usuarios().get(r.idUsuario());
                System.out.println("=================================");
                System.out.println("EMAIL SIMULADO");
                System.out.println("Para: " + u.email());
                System.out.println("Asunto: Recordatorio Reserva");
                System.out.println("Mensaje: Le recordamos su reserva de hoy día " + hoy + " a las " + r.horaInicio() +"h. Dispondrá de " + r.duracionMinutos() + " minutos de uso.");
                System.out.println("=================================");
            }
        }
    }

    @Scheduled(cron = "@monthly")
    public void correoMensual(){

    }
}

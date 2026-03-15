package edu.comillas.icai.gitt.pat.spring.practica_final.controlador;

import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioPistas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioReservas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ReservasControlador {
    @Autowired
    ServicioPistas servicioPistas;
    @Autowired
    ServicioUsuarios servicioUsuarios;
    @Autowired
    ServicioReservas servicioReservas;

    @PostMapping("/pistaPadel/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crearReserva(@Valid @RequestBody ReservaBody reserva, Authentication authentication) {
        return servicioReservas.crearReserva(reserva, authentication);
    }

    @GetMapping("/pistaPadel/reservations")
    public List<Reserva> misReservas(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        return servicioReservas.misReservas(from, to, authentication);
    }

    @GetMapping("/pistaPadel/reservations/{reservationId}")
    public Reserva obtenerReserva(@PathVariable int reservationId, Authentication authentication) {
        return servicioReservas.obtenerReserva(reservationId, authentication);
    }

    @DeleteMapping("/pistaPadel/reservations/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarReserva(@PathVariable int reservationId, Authentication authentication) {
        servicioReservas.cancelarReserva(reservationId, authentication);
    }

    @PatchMapping("/pistaPadel/reservations/{idReserva}")
    public ResponseEntity<Reserva> modificarReserva(
            @PathVariable int idReserva,
            @RequestBody @Valid ReservaBody reservaCambio,
            Authentication authentication
    ) {
        return servicioReservas.modificarReserva(idReserva, reservaCambio, authentication);
    }

    @GetMapping("/pistaPadel/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reserva>> getReservas(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) Integer pista,
            @RequestParam(required = false) Integer user) {
        return servicioReservas.getReservas(fecha, pista, user);
    }
}

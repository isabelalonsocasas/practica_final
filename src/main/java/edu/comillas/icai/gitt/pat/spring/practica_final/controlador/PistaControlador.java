package edu.comillas.icai.gitt.pat.spring.practica_final.controlador;

import edu.comillas.icai.gitt.pat.spring.practica_final.entidad.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioPistas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioReservas;
import edu.comillas.icai.gitt.pat.spring.practica_final.servicio.ServicioUsuarios;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class PistaControlador {
    @Autowired
    ServicioPistas servicioPistas;
    @Autowired
    ServicioUsuarios servicioUsuarios;
    @Autowired
    ServicioReservas servicioReservas;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pistaPadel/courts")
    public ResponseEntity<Pista> crearPista(@Valid @RequestBody Pista pista) {
        return servicioPistas.crearPista(pista);
    }

    @GetMapping("/pistaPadel/courts")
    public List<Pista> listarPistas(@RequestParam(required = false) Boolean active) {
        return servicioPistas.listarPistas(active);
    }

    @GetMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Pista> getInfoPista(@PathVariable Integer courtId) {
        return servicioPistas.getInfoPista(courtId);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Pista> actualizarPista(@PathVariable int courtId,
                                                @Valid @RequestBody Pista datosActualizados) {
        return servicioPistas.actualizarPista(courtId, datosActualizados);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/pistaPadel/courts/{courtId}")
    public ResponseEntity<Void> desactivarPista(@PathVariable int courtId) {
        return servicioPistas.desactivarPista(courtId);
    }

    @GetMapping("/pistaPadel/availability")
    public List<Map<String, Object>> consultarDisponibilidad(
            @RequestParam String date,
            @RequestParam(required = false) Integer courtId) {
        return servicioPistas.consultarDisponibilidad(date, courtId);
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public Map<String, Object> consultarDisponibilidadPista(
            @RequestParam String date,
            @PathVariable Integer courtId) {
        return servicioPistas.consultarDisponibilidadPista(date, courtId);
    }

    
}

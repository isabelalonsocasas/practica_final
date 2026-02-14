package edu.comillas.icai.gitt.pat.spring.practica_final;

import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Pista;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Reserva;
import edu.comillas.icai.gitt.pat.spring.practica_final.RECORDS.Usuario;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service

public class AlmacenDatos
{

    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Integer, Pista> pistas = new HashMap<>();
    private final Map<Integer, Reserva> reservas = new HashMap<>();

    public Map<Integer, Usuario> usuarios() {
            return usuarios;
    }

    public Map<Integer, Pista> pistas() {
            return pistas;
    }

    public Map<Integer, Reserva> reservas() {
            return reservas;
    }

    //Metodo para el log in
    public Usuario buscarPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

}

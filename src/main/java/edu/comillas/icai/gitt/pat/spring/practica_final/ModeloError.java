package edu.comillas.icai.gitt.pat.spring.practica_final;

import org.springframework.validation.FieldError;

import java.time.OffsetDateTime;
import java.util.List;

public record ModeloError(
        int status, //Código de HTTP
        String message,
        String path,
        OffsetDateTime time,
        List<FieldError> errorescampo)
 {
    public record FieldError (String field, String message) {}

}

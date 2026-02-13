package edu.comillas.icai.gitt.pat.spring.practica_final;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ControladorGlobalDeErrores {

    private Object err;

    //Errores lanzados por nosotros con throw new...
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ModeloError> ControladorResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        //ResponseEntity<ModeloError> vamos a devolver un codigo HTTP pero con un body personalizado con la clase ModeloError

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value()); //Coge el código HTTP de ex y se convierte en HttpStatus para usarlo en la respuesta

        //Con la ayuda de la clase ModeloError personalizamos nuestro error
        ModeloError bodyerror = new ModeloError (
                status.value(),
                ex.getReason(),
                request.getRequestURI(), //Donde ocurrio el error
                OffsetDateTime.now(),
                null);
        return ResponseEntity.status(status).body(bodyerror);
    }


    //Errores lanzados por @Valid, se lanza automaticamente MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ModeloError> ControladorMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        //Creamos la lista personalizada de los errores de campo
        List<ModeloError.FieldError> listaerror = ex.getBindingResult() //Todos los errores detectados
                .getFieldErrors()//Solo los errores de campo
                .stream()//Recorrer error por error
                .map(err -> new ModeloError.FieldError(err.getField(),err.getDefaultMessage())) //Personalizamos
                .toList(); //Los convertimos en lista

        //Personalizamos la salida body del error/errores
        ModeloError bodyerror = new ModeloError (
                status.value(),
                "Datos invaliddos",
                request.getRequestURI(),
                OffsetDateTime.now(),
                listaerror
        );
        return ResponseEntity.status(status).body(bodyerror);
    }

    //Errores generales internos dek sistema 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ModeloError> ControladorGeneralException (ResponseStatusException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; //Algo ha fallado dentro del servidor y no es un error controlado por defecto 500

        //Con la ayuda de la clase ModeloError personalizamos nuestro error
        ModeloError bodyerror = new ModeloError (
                status.value(),
                "Error servidor",
                request.getRequestURI(), //Donde ocurrio el error
                OffsetDateTime.now(),
                null);
        return ResponseEntity.status(status).body(bodyerror);
    }

    //Errores cuando El JSON esta mal formado, errores de tipo 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ModeloError> manejarJsonMal(HttpMessageNotReadableException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ModeloError bodyerror = new ModeloError(
                status.value(),
                "JSON inválido",
                request.getRequestURI(),
                OffsetDateTime.now(),
                null
        );

        return ResponseEntity.status(status).body(bodyerror);
    }

}

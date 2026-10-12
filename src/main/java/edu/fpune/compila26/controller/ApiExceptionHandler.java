package edu.fpune.compila26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.fpune.compila26.dto.ErrorResponse;
import edu.fpune.compila26.service.CodigoFuenteDemasiadoGrandeException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CodigoFuenteDemasiadoGrandeException.class)
    public ResponseEntity<ErrorResponse> codigoFuenteDemasiadoGrande(
            CodigoFuenteDemasiadoGrandeException exception) {
        ErrorResponse error = new ErrorResponse(
                "CODIGO_DEMASIADO_GRANDE",
                exception.getMessage(),
                "Caracteres recibidos: " + exception.getCaracteresRecibidos()
                        + "; m\u00e1ximo permitido: " + exception.getMaximoCaracteres() + "."
        );
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(error);
    }
}

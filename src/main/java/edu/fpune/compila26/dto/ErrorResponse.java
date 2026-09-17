package edu.fpune.compila26.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonProperty("exitoso")
        boolean exitoso,

        @JsonProperty("codigo_error")
        String codigoError,

        @JsonProperty("mensaje")
        String mensaje,

        @JsonProperty("timestamp")
        LocalDateTime timestamp,

        @JsonProperty("detalles")
        String detalles
) {
    public ErrorResponse(String codigoError, String mensaje, String detalles) {
        this(false, codigoError, mensaje, LocalDateTime.now(), detalles);
    }
}
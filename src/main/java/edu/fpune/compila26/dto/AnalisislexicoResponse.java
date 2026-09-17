package edu.fpune.compila26.dto;



import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.fpune.compila26.lexico.TokenDto;

public record AnalisislexicoResponse(
        @JsonProperty("exitoso")
        boolean exitoso,

        @JsonProperty("mensaje")
        String mensaje,

        @JsonProperty("tokens")
        List<TokenDto> tokens,

        @JsonProperty("cantidad")
        int cantidad,

        @JsonProperty("duracion_ms")
        long duracionMs
) {}
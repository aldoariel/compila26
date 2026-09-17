package edu.fpune.compila26.lexico;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDto(
        @JsonProperty("tipo")
        String tipo,

        @JsonProperty("valor")
        String valor,

        @JsonProperty("linea")
        int linea,

        @JsonProperty("columna")
        int columna
) {}
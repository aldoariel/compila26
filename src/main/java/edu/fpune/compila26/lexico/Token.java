package edu.fpune.compila26.lexico;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un token generado por el analizador léxico.
 * Versión migrada de Compila26 a Compila25-Spring.
 *
 * @author Compila26 Original
 * @version 2.0 (Java 21 + Spring Boot 3)
 */
public class Token {
    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("valor")
    private String valor;

    @JsonProperty("linea")
    private int linea;

    @JsonProperty("columna")
    private int columna;

    /**
     * Constructor básico - solo tipo y valor.
     * Línea y columna tienen valor por defecto (0).
     */
    public Token(String tipo, String valor) {
        super();
        this.tipo = tipo;
        this.valor = valor;
    }

    /**
     * Constructor completo - con línea y columna.
     */
    public Token(String tipo, String valor, int linea, int columna) {
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
        this.columna = columna;
    }

    /**
     * Constructor vacío (necesario para frameworks de serialización).
     */
    public Token() {
    }

    // ============ GETTERS ============
    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    // ============ SETTERS ============
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }
}
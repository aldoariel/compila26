package edu.fpune.compila26.lexico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analizador Léxico - Migrado de Compila26
 * Tokeniza código fuente y genera una lista de tokens.
 *
 * @author Compila25 Team
 * @version 2.0 (Java 21 + Spring Boot 3)
 */
public class Lexer {
    private int linea = 1;
    private int columna = 1;
    private String codigoFuente;

    // Palabras clave del lenguaje
    private static final Set<String> PALABRAS_CLAVE = new HashSet<>(Arrays.asList(
            "int", "if", "else", "while", "return"
    ));

    // Expresión regular con grupos nombrados para tipos de tokens
    private static final Pattern patron = Pattern.compile(
            "(?<PALABRA>[a-zA-Z_][a-zA-Z_0-9]*)|" +
                    "(?<NUMERO>\\d+)|" +
                    "(?<OPERADOR>>=|<=|==|!=|>|<|[+\\-*/=])|" +
                    "(?<DELIMITADOR>[;(){}])|" +
                    "(?<ESPACIO>\\s+)|" +
                    "(?<ERROR>.)"
    );

    /**
     * Constructor con código fuente.
     * @param codigoFuente Código a analizar
     */
    public Lexer(String codigoFuente) {
        this.linea = 1;
        this.columna = 1;
        this.codigoFuente = codigoFuente;
    }

    /**
     * Constructor sin parámetros.
     */
    public Lexer() {
        this.linea = 1;
        this.columna = 1;
    }

    /**
     * Analiza el código fuente y retorna una lista de tokens.
     * @param codigoFuente El código a tokenizar
     * @return Lista de tokens generados
     */
    public List<Token> analizar(String codigoFuente) {
        this.codigoFuente = codigoFuente;
        this.linea = 1;
        this.columna = 1;

        List<Token> tokens = new ArrayList<>();
        Matcher matcher = patron.matcher(codigoFuente);

        while (matcher.find()) {
            // PALABRA (identificador o palabra clave)
            if (matcher.group("PALABRA") != null) {
                String palabra = matcher.group();
                if (PALABRAS_CLAVE.contains(palabra)) {
                    tokens.add(new Token("PALABRA_CLAVE", palabra, linea, columna));
                } else {
                    tokens.add(new Token("IDENTIFICADOR", palabra, linea, columna));
                }
            }
            // NÚMERO (entero o flotante)
            else if (matcher.group("NUMERO") != null) {
                tokens.add(new Token("NUMERO", matcher.group(), linea, columna));
            }
            // OPERADOR (aritmético, comparación, asignación)
            else if (matcher.group("OPERADOR") != null) {
                tokens.add(new Token("OPERADOR", matcher.group(), linea, columna));
            }
            // DELIMITADOR (paréntesis, llaves, punto y coma)
            else if (matcher.group("DELIMITADOR") != null) {
                tokens.add(new Token("DELIMITADOR", matcher.group(), linea, columna));
            }
            // ERROR (carácter no reconocido)
            else if (matcher.group("ERROR") != null) {
                tokens.add(new Token("ERROR", matcher.group(), linea, columna));
            }
            // El grupo ESPACIO se ignora para no generar tokens innecesarios

            // Actualizar posición (línea y columna)
            actualizarPosicion(matcher.group());
        }

        return tokens;
    }

    /**
     * Actualiza la posición (línea y columna) después de procesar cada token.
     * @param token El token procesado
     */
    private void actualizarPosicion(String token) {
        if (token != null) {
            for (char c : token.toCharArray()) {
                if (c == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
            }
        }
    }

    /**
     * Obtiene el código fuente analizado.
     * @return Código fuente
     */
    public String getCodigoFuente() {
        return codigoFuente;
    }

    /**
     * Obtiene la línea actual.
     * @return Número de línea
     */
    public int getLinea() {
        return linea;
    }

    /**
     * Obtiene la columna actual.
     * @return Número de columna
     */
    public int getColumna() {
        return columna;
    }
}

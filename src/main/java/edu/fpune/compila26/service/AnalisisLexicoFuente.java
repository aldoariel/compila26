package edu.fpune.compila26.service;

import java.util.List;

import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;

final class AnalisisLexicoFuente {

    private AnalisisLexicoFuente() {
    }

    static Resultado analizar(String codigoFuente) {
        List<Token> tokens = new Lexer().analizar(codigoFuente);
        List<String> errores = tokens.stream()
                .filter(token -> "ERROR".equals(token.getTipo()))
                .map(AnalisisLexicoFuente::formatearError)
                .toList();
        return new Resultado(tokens, errores);
    }

    private static String formatearError(Token token) {
        return "Error l\u00e9xico: car\u00e1cter no reconocido '"
                + token.getValor()
                + "' en l\u00ednea "
                + token.getLinea()
                + ", columna "
                + token.getColumna()
                + ".";
    }

    record Resultado(List<Token> tokens, List<String> errores) {
        boolean tieneErrores() {
            return !errores.isEmpty();
        }
    }
}

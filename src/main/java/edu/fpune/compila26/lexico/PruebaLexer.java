package edu.fpune.compila26.lexico;

import java.util.List;

public class PruebaLexer {

    public static void main(String[] args) {

        String codigo = "if (x >= 10) return x + 1;";

        Lexer lexer = new Lexer();

        List<Token> tokens = lexer.analizar(codigo);

        for (Token token : tokens) {
            System.out.println(
                    token.getTipo() + " -> " + token.getValor()
            );
        }
    }
}
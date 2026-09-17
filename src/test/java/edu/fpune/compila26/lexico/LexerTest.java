package edu.fpune.compila26.lexico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class LexerTest {

    @Test
    void reconocerIdentificadorSimple() {
        List<Token> tokens = new Lexer().analizar("variable");

        assertToken(tokens.get(0), "IDENTIFICADOR", "variable", 1, 1);
    }

    @Test
    void reconocerTodasLasPalabrasClaveSoportadas() {
        List<Token> tokens = new Lexer().analizar("int if else while return");

        assertEquals(List.of("int", "if", "else", "while", "return"),
                tokens.stream().map(Token::getValor).toList());
        assertTrue(tokens.stream().allMatch(token -> token.getTipo().equals("PALABRA_CLAVE")));
    }

    @Test
    void reconocerNumeroEntero() {
        List<Token> tokens = new Lexer().analizar("12345");

        assertToken(tokens.get(0), "NUMERO", "12345", 1, 1);
    }

    @Test
    void reconocerOperadoresAritmeticosYAsignacion() {
        List<Token> tokens = new Lexer().analizar("+ - * / =");

        assertEquals(List.of("+", "-", "*", "/", "="),
                tokens.stream().map(Token::getValor).toList());
        assertTrue(tokens.stream().allMatch(token -> token.getTipo().equals("OPERADOR")));
    }

    @Test
    void reconocerOperadoresRelacionalesSoportados() {
        List<Token> tokens = new Lexer().analizar(">= <= == != > <");

        assertEquals(List.of(">=", "<=", "==", "!=", ">", "<"),
                tokens.stream().map(Token::getValor).toList());
        assertTrue(tokens.stream().allMatch(token -> token.getTipo().equals("OPERADOR")));
    }

    @Test
    void reconocerDelimitadoresSoportados() {
        List<Token> tokens = new Lexer().analizar(";(){}");

        assertEquals(List.of(";", "(", ")", "{", "}"),
                tokens.stream().map(Token::getValor).toList());
        assertTrue(tokens.stream().allMatch(token -> token.getTipo().equals("DELIMITADOR")));
    }

    @Test
    void ignorarEspaciosEnBlancoConservandoColumnas() {
        List<Token> tokens = new Lexer().analizar("x   =  10");

        assertEquals(3, tokens.size());
        assertToken(tokens.get(0), "IDENTIFICADOR", "x", 1, 1);
        assertToken(tokens.get(1), "OPERADOR", "=", 1, 5);
        assertToken(tokens.get(2), "NUMERO", "10", 1, 8);
    }

    @Test
    void actualizarLineaYColumnaDespuesDeUnSaltoDeLinea() {
        List<Token> tokens = new Lexer().analizar("x = 10;\ny=2;");

        assertToken(tokens.get(4), "IDENTIFICADOR", "y", 2, 1);
        assertToken(tokens.get(5), "OPERADOR", "=", 2, 2);
        assertToken(tokens.get(6), "NUMERO", "2", 2, 3);
        assertToken(tokens.get(7), "DELIMITADOR", ";", 2, 4);
    }

    @Test
    void generarTokenErrorParaCaracterInvalido() {
        List<Token> tokens = new Lexer().analizar("@");

        assertToken(tokens.get(0), "ERROR", "@", 1, 1);
    }

    @Test
    void reiniciarEstadoEnLlamadasConsecutivas() {
        Lexer lexer = new Lexer();
        lexer.analizar("x\ny");

        List<Token> segundaEjecucion = lexer.analizar("z");

        assertEquals("z", lexer.getCodigoFuente());
        assertToken(segundaEjecucion.get(0), "IDENTIFICADOR", "z", 1, 1);
        assertEquals(1, lexer.getLinea());
        assertEquals(2, lexer.getColumna());
    }

    private void assertToken(Token token, String tipo, String valor, int linea, int columna) {
        assertEquals(tipo, token.getTipo());
        assertEquals(valor, token.getValor());
        assertEquals(linea, token.getLinea());
        assertEquals(columna, token.getColumna());
    }
}

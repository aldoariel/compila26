package edu.fpune.compila26.sintactico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.lexico.Lexer;

class ParserTest {

    @Test
    void analizarDeclaracionExplicita() {
        Resultado resultado = analizar("int x;");

        assertTrue(resultado.parser().esExitoso());
        NodoAST declaracion = resultado.ast().getHijos().get(0);
        assertNodo(declaracion, "DECLARACION", "int");
        assertNodo(declaracion.getHijos().get(0), "ID", "x");
    }

    @Test
    void analizarDeclaracionesYAsignacionesEnOrden() {
        Resultado resultado = analizar("int x; int y; x = 10; y = x + 5;");

        assertTrue(resultado.parser().esExitoso());
        assertEquals(4, resultado.ast().getHijos().size());
        assertEquals("DECLARACION", resultado.ast().getHijos().get(0).getTipo());
        assertEquals("DECLARACION", resultado.ast().getHijos().get(1).getTipo());
        assertEquals("ASIGNACION", resultado.ast().getHijos().get(2).getTipo());
        assertEquals("ASIGNACION", resultado.ast().getHijos().get(3).getTipo());
    }

    @Test
    void analizarAsignacionSimple() {
        Resultado resultado = analizar("x = 10;");

        assertTrue(resultado.parser().esExitoso());
        NodoAST asignacion = resultado.ast().getHijos().get(0);
        assertEquals("ASIGNACION", asignacion.getTipo());
        assertNodo(asignacion.getHijos().get(0), "ID", "x");
        assertNodo(asignacion.getHijos().get(1), "NUMERO", "10");
    }

    @Test
    void analizarExpresionAritmeticaSimple() {
        Resultado resultado = analizar("x = 5 + 2;");

        NodoAST suma = expresionDePrimeraAsignacion(resultado.ast());
        assertNodo(suma, "OPERADOR", "+");
        assertNodo(suma.getHijos().get(0), "NUMERO", "5");
        assertNodo(suma.getHijos().get(1), "NUMERO", "2");
    }

    @Test
    void representarMultiplicacionDebajoDeLaSumaPorPrecedencia() {
        Resultado resultado = analizar("x = 10 + 5 * 2;");

        NodoAST suma = expresionDePrimeraAsignacion(resultado.ast());
        NodoAST multiplicacion = suma.getHijos().get(1);
        assertNodo(suma, "OPERADOR", "+");
        assertNodo(multiplicacion, "OPERADOR", "*");
        assertNodo(multiplicacion.getHijos().get(0), "NUMERO", "5");
        assertNodo(multiplicacion.getHijos().get(1), "NUMERO", "2");
    }

    @Test
    void representarParentesisAntesDeLaMultiplicacion() {
        Resultado resultado = analizar("x = (10 + 5) * 2;");

        NodoAST multiplicacion = expresionDePrimeraAsignacion(resultado.ast());
        NodoAST suma = multiplicacion.getHijos().get(0);
        assertNodo(multiplicacion, "OPERADOR", "*");
        assertNodo(suma, "OPERADOR", "+");
    }

    @Test
    void asociarRestasDeIzquierdaADerecha() {
        Resultado resultado = analizar("x = 10 - 5 - 2;");

        NodoAST restaExterior = expresionDePrimeraAsignacion(resultado.ast());
        NodoAST restaInterior = restaExterior.getHijos().get(0);
        assertNodo(restaExterior, "OPERADOR", "-");
        assertNodo(restaInterior, "OPERADOR", "-");
        assertNodo(restaExterior.getHijos().get(1), "NUMERO", "2");
    }

    @Test
    void analizarMultiplesSentenciasEnOrden() {
        Resultado resultado = analizar("x = 1; y = x + 2;");

        assertTrue(resultado.parser().esExitoso());
        assertEquals(2, resultado.ast().getHijos().size());
        assertEquals("x", resultado.ast().getHijos().get(0).getHijos().get(0).getValor());
        assertEquals("y", resultado.ast().getHijos().get(1).getHijos().get(0).getValor());
    }

    @Test
    void producirAstParcialParaExpresionIncompleta() {
        Resultado resultado = analizar("x = 10 +");

        assertFalse(resultado.parser().esExitoso());
        assertEquals(2, resultado.parser().getErrores().size());
        assertTrue(resultado.parser().getErrores().get(0)
                .contains("al final de la entrada (EOF), l\u00ednea 1, columna 9"));
        NodoAST suma = expresionDePrimeraAsignacion(resultado.ast());
        assertNodo(suma, "OPERADOR", "+");
        assertEquals("ERROR", suma.getHijos().get(1).getTipo());
    }

    @Test
    void ubicarFactorInvalidoEnLineaYColumna() {
        Resultado resultado = analizar("int x;\nx = 10 + ;");

        assertFalse(resultado.parser().esExitoso());
        assertTrue(resultado.parser().getErrores().get(0)
                .contains("cerca de ';' en l\u00ednea 2, columna 10"));
    }

    @Test
    void ubicarParentesisFaltanteEnElTokenActual() {
        Resultado resultado = analizar("int x;\nx = (10 + 5;");

        assertFalse(resultado.parser().esExitoso());
        assertTrue(resultado.parser().getErrores().stream()
                .anyMatch(error -> error.contains("Se esperaba ')' cerca de ';' en l\u00ednea 2, columna 12")));
    }

    @Test
    void ubicarPuntoYComaFaltanteAlInicioDeLaSiguienteLinea() {
        Resultado resultado = analizar("int x\nx = 10;");

        assertFalse(resultado.parser().esExitoso());
        assertTrue(resultado.parser().getErrores().get(0)
                .contains("Se esperaba ';' cerca de 'x' en l\u00ednea 2, columna 1"));
    }

    @Test
    void registrarErrorAnteFactorInvalido() {
        Resultado resultado = analizar("x = @;");

        assertFalse(resultado.parser().esExitoso());
        assertTrue(resultado.parser().getErrores().get(0).contains("Factor inválido: @"));
        assertEquals("ERROR", expresionDePrimeraAsignacion(resultado.ast()).getTipo());
    }

    @Test
    void recuperarEnPuntoYComaYContinuarConLaSiguienteSentenciaDisponible() {
        Resultado resultado = analizar("x = 1 y = 2; z = 3;");

        assertFalse(resultado.parser().esExitoso());
        assertEquals(2, resultado.ast().getHijos().size());
        assertEquals("x", resultado.ast().getHijos().get(0).getHijos().get(0).getValor());
        assertEquals("z", resultado.ast().getHijos().get(1).getHijos().get(0).getValor());
    }

    @Test
    void devolverProgramaVacioParaListaDeTokensVacia() {
        Resultado resultado = analizar("");

        assertTrue(resultado.parser().esExitoso());
        assertEquals("PROGRAMA", resultado.ast().getTipo());
        assertTrue(resultado.ast().getHijos().isEmpty());
    }

    private Resultado analizar(String codigo) {
        Parser parser = new Parser(new Lexer().analizar(codigo));
        return new Resultado(parser, parser.parse());
    }

    private NodoAST expresionDePrimeraAsignacion(NodoAST programa) {
        return programa.getHijos().get(0).getHijos().get(1);
    }

    private void assertNodo(NodoAST nodo, String tipo, String valor) {
        assertEquals(tipo, nodo.getTipo());
        assertEquals(valor, nodo.getValor());
    }

    private record Resultado(Parser parser, NodoAST ast) {
    }
}

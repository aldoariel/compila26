package edu.fpune.compila26.semantico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.sintactico.NodoAST;

class AnalizadorSemanticoTest {

    @Test
    void rechazarAstNulo() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        assertFalse(analizador.analizar(null));
        assertEquals(List.of("El AST es nulo."), analizador.getErrores());
    }

    @Test
    void aceptarDeclaracionExplicitaYAsignacionValida() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        boolean resultado = analizador.analizar(programa(
                declaracion("int", "x"),
                asignacion("x", numero("10"))
        ));

        assertTrue(resultado);
        assertTrue(analizador.getTabla().estaDeclarado("x"));
        assertEquals("int", analizador.getTabla().obtenerTipo("x"));
    }

    @Test
    void aceptarUsoPosteriorDeVariableDeclarada() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();
        NodoAST programa = programa(
                declaracion("int", "x"),
                declaracion("int", "y"),
                asignacion("x", numero("10")),
                asignacion("y", operador("+", identificador("x"), numero("5")))
        );

        assertTrue(analizador.analizar(programa));
        assertEquals("int", analizador.getTabla().obtenerTipo("x"));
        assertEquals("int", analizador.getTabla().obtenerTipo("y"));
    }

    @Test
    void informarUsoDeVariableNoDeclarada() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        boolean resultado = analizador.analizar(
                programa(
                        declaracion("int", "x"),
                        asignacion("x", operador("+", identificador("y"), numero("10")))
                )
        );

        assertFalse(resultado);
        assertEquals(List.of("Variable no declarada: 'y'"), analizador.getErrores());
        assertTrue(analizador.getTabla().estaDeclarado("x"));
    }

    @Test
    void inferirTipoIntParaOperacionAritmeticaValida() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        boolean resultado = analizador.analizar(
                programa(
                        declaracion("int", "resultado"),
                        asignacion("resultado", operador("*", numero("5"), numero("2")))
                )
        );

        assertTrue(resultado);
        assertEquals("int", analizador.getTabla().obtenerTipo("resultado"));
    }

    @Test
    void informarAsignacionIncompleta() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();
        NodoAST asignacion = new NodoAST("ASIGNACION");
        asignacion.agregarHijo(identificador("x"));

        assertFalse(analizador.analizar(asignacion));
        assertEquals(List.of("Asignación incompleta."), analizador.getErrores());
    }

    @Test
    void informarOperadorIncompletoSinDeclararDestino() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();
        NodoAST operador = new NodoAST("OPERADOR", "+");
        operador.agregarHijo(numero("1"));

        assertFalse(analizador.analizar(programa(
                declaracion("int", "x"),
                asignacion("x", operador)
        )));
        assertEquals(List.of("Operador '+' incompleto."), analizador.getErrores());
        assertEquals("int", analizador.getTabla().obtenerTipo("x"));
    }

    @Test
    void informarNodoDesconocido() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        assertFalse(analizador.analizar(new NodoAST("DESCONOCIDO")));
        assertEquals(List.of("Nodo semántico no reconocido: DESCONOCIDO"), analizador.getErrores());
    }

    @Test
    void evitarErrorAdicionalDeOperadorCuandoUnOperandoEsDesconocido() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        analizador.analizar(programa(
                declaracion("int", "x"),
                asignacion("x", operador("+", identificador("y"), numero("10")))
        ));

        assertEquals(1, analizador.getErrores().size());
        assertEquals("Variable no declarada: 'y'", analizador.getErrores().get(0));
    }

    @Test
    void analizarTodasLasOperacionesAritmeticasComoEnteros() {
        for (String simbolo : List.of("+", "-", "*", "/")) {
            AnalizadorSemantico analizador = new AnalizadorSemantico();

            assertTrue(analizador.analizar(programa(
                    declaracion("int", "x"),
                    asignacion("x", operador(simbolo, numero("8"), numero("2")))
            )));
            assertEquals("int", analizador.getTabla().obtenerTipo("x"));
        }
    }

    @Test
    void informarAsignacionAVariableNoDeclarada() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        assertFalse(analizador.analizar(asignacion("x", numero("10"))));
        assertEquals(List.of("Variable no declarada: 'x'"), analizador.getErrores());
        assertNull(analizador.getTabla().obtener("x"));
    }

    @Test
    void informarRedeclaracionDeVariable() {
        AnalizadorSemantico analizador = new AnalizadorSemantico();

        assertFalse(analizador.analizar(programa(
                declaracion("int", "x"),
                declaracion("int", "x")
        )));
        assertEquals(List.of("Variable 'x' ya declarada."), analizador.getErrores());
        assertEquals(1, analizador.getTabla().cantidad());
    }

    private NodoAST programa(NodoAST... sentencias) {
        NodoAST programa = new NodoAST("PROGRAMA");
        for (NodoAST sentencia : sentencias) {
            programa.agregarHijo(sentencia);
        }
        return programa;
    }

    private NodoAST asignacion(String variable, NodoAST expresion) {
        NodoAST asignacion = new NodoAST("ASIGNACION");
        asignacion.agregarHijo(identificador(variable));
        asignacion.agregarHijo(expresion);
        return asignacion;
    }

    private NodoAST declaracion(String tipo, String variable) {
        NodoAST declaracion = new NodoAST("DECLARACION", tipo);
        declaracion.agregarHijo(identificador(variable));
        return declaracion;
    }

    private NodoAST operador(String simbolo, NodoAST izquierdo, NodoAST derecho) {
        NodoAST operador = new NodoAST("OPERADOR", simbolo);
        operador.agregarHijo(izquierdo);
        operador.agregarHijo(derecho);
        return operador;
    }

    private NodoAST identificador(String nombre) {
        return new NodoAST("ID", nombre);
    }

    private NodoAST numero(String valor) {
        return new NodoAST("NUMERO", valor);
    }
}

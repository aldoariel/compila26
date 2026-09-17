package edu.fpune.compila26.intermedio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.sintactico.NodoAST;

class GeneradorIntermedioTest {

    @Test
    void generarConAstNuloDevuelveListaVacia() {
        GeneradorIntermedio generador = new GeneradorIntermedio();

        List<String> resultado = generador.generar(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void generarProgramaVacioDevuelveListaVacia() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST programa = new NodoAST("PROGRAMA");

        List<String> resultado = generador.generar(programa);

        assertEquals(List.of(), resultado);
    }

    @Test
    void ignorarDeclaracionYGenerarSolamenteLaAsignacion() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST programa = new NodoAST("PROGRAMA");
        NodoAST declaracion = new NodoAST("DECLARACION", "int");
        declaracion.agregarHijo(identificador("x"));
        programa.agregarHijo(declaracion);
        programa.agregarHijo(asignacion("x", numero("10")));

        List<String> resultado = generador.generar(programa);

        assertEquals(List.of("x = 10"), resultado);
    }

    @Test
    void generarAsignacionDeConstante() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST asignacion = asignacion("x", numero("10"));

        List<String> resultado = generador.generar(asignacion);

        assertEquals(List.of("x = 10"), resultado);
    }

    @Test
    void generarAsignacionEntreVariables() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST asignacion = asignacion("y", identificador("x"));

        List<String> resultado = generador.generar(asignacion);

        assertEquals(List.of("y = x"), resultado);
    }

    @Test
    void generarOperacionAritmeticaSimpleAntesDeLaAsignacion() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST asignacion = asignacion("x", operador("+", numero("5"), numero("2")));

        List<String> resultado = generador.generar(asignacion);

        assertEquals(List.of(
                "t0 = 5 + 2",
                "x = t0"
        ), resultado);
    }

    @Test
    void respetarPrecedenciaRepresentadaPorLaEstructuraDelAst() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST multiplicacion = operador("*", numero("5"), numero("2"));
        NodoAST suma = operador("+", numero("10"), multiplicacion);
        NodoAST asignacion = asignacion("x", suma);

        List<String> resultado = generador.generar(asignacion);

        assertEquals(List.of(
                "t0 = 5 * 2",
                "t1 = 10 + t0",
                "x = t1"
        ), resultado);
    }

    @Test
    void generarOperacionesAnidadasEnOrdenPostorden() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST suma = operador("+", numero("2"), numero("3"));
        NodoAST resta = operador("-", numero("4"), numero("1"));
        NodoAST multiplicacion = operador("*", suma, resta);

        List<String> resultado = generador.generar(asignacion("x", multiplicacion));

        assertEquals(List.of(
                "t0 = 2 + 3",
                "t1 = 4 - 1",
                "t2 = t0 * t1",
                "x = t2"
        ), resultado);
    }

    @Test
    void generarVariasAsignacionesEnElOrdenDelPrograma() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST programa = new NodoAST("PROGRAMA");
        programa.agregarHijo(asignacion("x", numero("10")));
        programa.agregarHijo(asignacion("y", operador("+", identificador("x"), numero("5"))));

        List<String> resultado = generador.generar(programa);

        assertEquals(List.of(
                "x = 10",
                "t0 = x + 5",
                "y = t0"
        ), resultado);
    }

    @Test
    void reiniciarTemporalesEnCadaGeneracion() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST primeraAsignacion = asignacion("x", operador("+", numero("1"), numero("2")));
        NodoAST segundaAsignacion = asignacion("y", operador("*", numero("3"), numero("4")));

        List<String> primerResultado = generador.generar(primeraAsignacion);
        List<String> segundoResultado = generador.generar(segundaAsignacion);

        assertEquals(List.of("t0 = 1 + 2", "x = t0"), primerResultado);
        assertEquals(List.of("t0 = 3 * 4", "y = t0"), segundoResultado);
    }

    @Test
    void generarConAstNoNuloDevuelveCopiaDefensiva() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        List<String> resultado = generador.generar(asignacion("x", numero("10")));

        resultado.add("instruccion externa");

        assertEquals(List.of("x = 10"), generador.getCodigoIntermedio());
    }

    @Test
    void ignorarNodoDesconocidoSinGenerarInstrucciones() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST nodoDesconocido = new NodoAST("DESCONOCIDO", "valor");

        List<String> resultado = generador.generar(nodoDesconocido);

        assertEquals(List.of(), resultado);
    }

    @Test
    void ignorarAsignacionYOperadorIncompletosSinLanzarExcepcion() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        NodoAST programa = new NodoAST("PROGRAMA");
        NodoAST asignacionIncompleta = new NodoAST("ASIGNACION");
        asignacionIncompleta.agregarHijo(identificador("x"));
        NodoAST operadorIncompleto = new NodoAST("OPERADOR", "+");
        operadorIncompleto.agregarHijo(numero("1"));
        programa.agregarHijo(asignacionIncompleta);
        programa.agregarHijo(operadorIncompleto);

        List<String> resultado = generador.generar(programa);

        assertEquals(List.of(), resultado);
    }

    private NodoAST asignacion(String variable, NodoAST expresion) {
        NodoAST asignacion = new NodoAST("ASIGNACION");
        asignacion.agregarHijo(identificador(variable));
        asignacion.agregarHijo(expresion);
        return asignacion;
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

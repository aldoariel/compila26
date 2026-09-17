package edu.fpune.compila26.intermedio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.sintactico.NodoAST;

class InstruccionIntermediaTest {

    @Test
    void representarAsignacionDeConstante() {
        InstruccionIntermedia instruccion = generar(asignacion("x", numero("10"))).get(0);

        assertEquals(OperacionIntermedia.ASIGNAR, instruccion.operacion());
        assertEquals(Operando.variable("x"), instruccion.destino());
        assertEquals(Operando.constante("10"), instruccion.operando1());
        assertNull(instruccion.operando2());
    }

    @Test
    void representarOperacionAritmeticaConTemporalYOperandosTipados() {
        List<InstruccionIntermedia> instrucciones =
                generar(asignacion("x", operador("+", numero("5"), numero("2"))));

        InstruccionIntermedia suma = instrucciones.get(0);
        assertEquals(OperacionIntermedia.SUMAR, suma.operacion());
        assertEquals(Operando.temporal("t0"), suma.destino());
        assertEquals(Operando.constante("5"), suma.operando1());
        assertEquals(Operando.constante("2"), suma.operando2());
    }

    @Test
    void representarExpresionAnidadaEnOrdenConTemporalesConsecutivos() {
        NodoAST multiplicacion = operador("*", numero("5"), numero("2"));
        NodoAST suma = operador("+", numero("10"), multiplicacion);

        List<InstruccionIntermedia> instrucciones = generar(asignacion("x", suma));

        assertEquals(3, instrucciones.size());
        assertEquals(OperacionIntermedia.MULTIPLICAR, instrucciones.get(0).operacion());
        assertEquals(Operando.temporal("t0"), instrucciones.get(0).destino());
        assertEquals(OperacionIntermedia.SUMAR, instrucciones.get(1).operacion());
        assertEquals(Operando.temporal("t1"), instrucciones.get(1).destino());
        assertEquals(Operando.temporal("t0"), instrucciones.get(1).operando2());
    }

    @Test
    void representarAsignacionDesdeTemporal() {
        List<InstruccionIntermedia> instrucciones =
                generar(asignacion("x", operador("+", numero("5"), numero("2"))));

        InstruccionIntermedia asignacion = instrucciones.get(1);
        assertEquals(OperacionIntermedia.ASIGNAR, asignacion.operacion());
        assertEquals(Operando.variable("x"), asignacion.destino());
        assertEquals(Operando.temporal("t0"), asignacion.operando1());
    }

    @Test
    void formatearInstruccionesComoElContratoTextualExistente() {
        NodoAST multiplicacion = operador("*", numero("5"), numero("2"));
        NodoAST suma = operador("+", numero("10"), multiplicacion);
        GeneradorIntermedio generador = new GeneradorIntermedio();

        List<InstruccionIntermedia> instrucciones =
                generador.generarInstrucciones(asignacion("x", suma));

        List<String> texto = instrucciones.stream()
                .map(InstruccionIntermedia::formatear)
                .toList();
        assertEquals(List.of(
                "t0 = 5 * 2",
                "t1 = 10 + t0",
                "x = t1"
        ), texto);
        assertEquals(texto, generador.getCodigoIntermedio());
    }

    @Test
    void devolverCopiaDefensivaDeLasInstrucciones() {
        GeneradorIntermedio generador = new GeneradorIntermedio();
        List<InstruccionIntermedia> resultado =
                generador.generarInstrucciones(asignacion("x", numero("10")));

        resultado.clear();

        assertEquals(1, generador.getInstrucciones().size());
        assertEquals("x = 10", generador.getInstrucciones().get(0).formatear());
    }

    private List<InstruccionIntermedia> generar(NodoAST ast) {
        return new GeneradorIntermedio().generarInstrucciones(ast);
    }

    private NodoAST asignacion(String variable, NodoAST expresion) {
        NodoAST asignacion = new NodoAST("ASIGNACION");
        asignacion.agregarHijo(new NodoAST("ID", variable));
        asignacion.agregarHijo(expresion);
        return asignacion;
    }

    private NodoAST operador(String simbolo, NodoAST izquierdo, NodoAST derecho) {
        NodoAST operador = new NodoAST("OPERADOR", simbolo);
        operador.agregarHijo(izquierdo);
        operador.agregarHijo(derecho);
        return operador;
    }

    private NodoAST numero(String valor) {
        return new NodoAST("NUMERO", valor);
    }
}

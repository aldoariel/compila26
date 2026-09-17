package edu.fpune.compila26.backend.neander;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.intermedio.Operando;
import edu.fpune.compila26.intermedio.OperacionIntermedia;

class GeneradorNeanderTest {

    private final GeneradorNeander generador = new GeneradorNeander();

    @Test
    void traducirAsignacionDeConstanteConDatoExplicito() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(
                InstruccionIntermedia.asignar(
                        Operando.variable("x"),
                        Operando.constante("10")
                )
        ));

        assertTrue(resultado.exitoso());
        assertEquals(List.of("LDA 81H", "STA 80H", "HLT"), resultado.ensamblador());
        assertEquals(Operando.variable("x"), resultado.datos().get(0).operando());
        assertEquals(Operando.constante("10"), resultado.datos().get(1).operando());
        assertEquals(10, resultado.datos().get(1).valorInicial());
    }

    @Test
    void traducirSumaYAsignacionConSecuenciaLdaAddSta() {
        InstruccionIntermedia suma = new InstruccionIntermedia(
                OperacionIntermedia.SUMAR,
                Operando.temporal("t0"),
                Operando.variable("a"),
                Operando.variable("b")
        );
        InstruccionIntermedia asignacion = InstruccionIntermedia.asignar(
                Operando.variable("x"),
                Operando.temporal("t0")
        );

        ResultadoGeneracionNeander resultado =
                generador.generar(List.of(suma, asignacion));

        assertEquals(List.of(
                "LDA 80H",
                "ADD 81H",
                "STA 83H",
                "LDA 83H",
                "STA 82H",
                "HLT"
        ), resultado.ensamblador());
    }

    @Test
    void conservarInstruccionYDireccionComoDatosEstructurados() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(
                InstruccionIntermedia.asignar(
                        Operando.variable("x"),
                        Operando.variable("y")
                )
        ));

        InstruccionEnsambladorNeander primera = resultado.instrucciones().get(0);
        assertEquals(InstruccionNeander.LDA, primera.instruccion());
        assertEquals(0x81, primera.direccion());
        assertEquals("LDA 81H", primera.formatear());
    }

    @Test
    void agregarHltAlFinalDeGeneracionExitosa() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(
                InstruccionIntermedia.asignar(
                        Operando.variable("x"),
                        Operando.variable("y")
                )
        ));

        assertEquals(InstruccionNeander.HLT,
                resultado.instrucciones().get(resultado.instrucciones().size() - 1).instruccion());
    }

    @Test
    void informarOperacionNoSoportadaSinGenerarEnsambladorParcial() {
        InstruccionIntermedia multiplicacion = new InstruccionIntermedia(
                OperacionIntermedia.MULTIPLICAR,
                Operando.temporal("t0"),
                Operando.constante("5"),
                Operando.constante("2")
        );

        ResultadoGeneracionNeander resultado = generador.generar(List.of(multiplicacion));

        assertFalse(resultado.exitoso());
        assertEquals(List.of(
                "Operación MULTIPLICAR todavía no soportada por el backend NEANDER."
        ), resultado.diagnosticos());
        assertTrue(resultado.instrucciones().isEmpty());
    }

    @Test
    void generarSoloHltParaEntradaVacia() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of());

        assertTrue(resultado.exitoso());
        assertEquals(List.of("HLT"), resultado.ensamblador());
        assertTrue(resultado.datos().isEmpty());
    }

    @Test
    void traducirRestaVariableMenosVariableMedianteComplementoADos() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(resta(
                Operando.temporal("t0"), Operando.variable("a"), Operando.variable("b"))));

        assertTrue(resultado.exitoso());
        assertEquals(List.of(
                "LDA 81H", "NOT", "ADD 83H", "STA 84H",
                "LDA 80H", "ADD 84H", "STA 82H", "HLT"
        ), resultado.ensamblador());
        assertEquals(1, resultado.datos().stream()
                .filter(dato -> Integer.valueOf(1).equals(dato.valorInicial())).count());
        assertTrue(resultado.datos().get(4).temporalInterno());
    }

    @Test
    void traducirRestaVariableMenosConstante() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(resta(
                Operando.temporal("t0"), Operando.variable("a"), Operando.constante("5"))));

        assertEquals(List.of(
                "LDA 82H", "NOT", "ADD 83H", "STA 84H",
                "LDA 80H", "ADD 84H", "STA 81H", "HLT"
        ), resultado.ensamblador());
    }

    @Test
    void traducirRestaConstanteMenosVariable() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(resta(
                Operando.temporal("t0"), Operando.constante("5"), Operando.variable("a"))));

        assertEquals(List.of(
                "LDA 80H", "NOT", "ADD 83H", "STA 84H",
                "LDA 82H", "ADD 84H", "STA 81H", "HLT"
        ), resultado.ensamblador());
    }

    @Test
    void traducirRestaConstanteMenosConstante() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(resta(
                Operando.temporal("t0"), Operando.constante("10"), Operando.constante("5"))));

        assertEquals(List.of(
                "LDA 82H", "NOT", "ADD 83H", "STA 84H",
                "LDA 81H", "ADD 84H", "STA 80H", "HLT"
        ), resultado.ensamblador());
    }

    @Test
    void reutilizarConstanteUnoYTemporalInternoEntreRestas() {
        ResultadoGeneracionNeander resultado = generador.generar(List.of(
                resta(Operando.temporal("t0"), Operando.variable("a"), Operando.variable("b")),
                resta(Operando.temporal("t1"), Operando.variable("a"), Operando.variable("b"))));

        assertEquals(1, resultado.datos().stream().filter(DatoNeander::temporalInterno).count());
        assertEquals(1, resultado.datos().stream()
                .filter(dato -> Integer.valueOf(1).equals(dato.valorInicial())).count());
        assertEquals(resultado.instrucciones().get(3).direccion(),
                resultado.instrucciones().get(10).direccion());
    }

    @Test
    void mantenerDivisionComoOperacionNoSoportada() {
        InstruccionIntermedia division = new InstruccionIntermedia(
                OperacionIntermedia.DIVIDIR,
                Operando.temporal("t0"), Operando.constante("6"), Operando.constante("2"));

        ResultadoGeneracionNeander resultado = generador.generar(List.of(division));

        assertFalse(resultado.exitoso());
        assertTrue(resultado.instrucciones().isEmpty());
        assertEquals(1, resultado.diagnosticos().size());
        assertTrue(resultado.diagnosticos().get(0).contains("DIVIDIR"));
    }

    @Test
    void convertirAgotamientoDeMemoriaEnDiagnostico() {
        List<InstruccionIntermedia> instrucciones = new ArrayList<>();
        for (int indice = 0; indice < 129; indice++) {
            instrucciones.add(InstruccionIntermedia.asignar(
                    Operando.variable("v" + indice),
                    Operando.variable("v" + indice)
            ));
        }

        ResultadoGeneracionNeander resultado = generador.generar(instrucciones);

        assertFalse(resultado.exitoso());
        assertEquals(List.of("Memoria de datos NEANDER agotada."),
                resultado.diagnosticos());
        assertTrue(resultado.instrucciones().isEmpty());
    }

    private InstruccionIntermedia resta(
            Operando destino,
            Operando operando1,
            Operando operando2) {
        return new InstruccionIntermedia(
                OperacionIntermedia.RESTAR, destino, operando1, operando2);
    }
}

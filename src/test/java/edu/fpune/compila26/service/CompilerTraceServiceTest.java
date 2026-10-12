package edu.fpune.compila26.service;

import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.CONSTRUIR_NODO;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.ERROR_SINTACTICO;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.FINALIZAR_RECUPERACION;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.INICIAR_RECUPERACION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.sintactico.PasoSintactico;

class CompilerTraceServiceTest {

    private final CompilerService service = new CompilerService();

    @Test
    void mantenerAusenteLaTrazaEnLaInvocacionAnterior() {
        CompilerService.ResultadoCompilacion resultado = service.compilar("int x;");

        assertNull(resultado.trazaSintactica());
        assertNull(resultado.trazaSintacticaTruncada());
    }

    @Test
    void mantenerAusenteLaTrazaCuandoSeDesactivaExpresamente() {
        CompilerService.ResultadoCompilacion resultado = service.compilar("int x;", false);

        assertNull(resultado.trazaSintactica());
        assertNull(resultado.trazaSintacticaTruncada());
    }

    @Test
    void devolverTrazaRealConPrecedenciaSinAlterarElResultado() {
        String codigo = "int x; x = 10 + 5 * 2;";
        CompilerService.ResultadoCompilacion sinTraza = service.compilar(codigo);
        CompilerService.ResultadoCompilacion conTraza = service.compilar(codigo, true);

        assertFalse(conTraza.trazaSintactica().isEmpty());
        assertFalse(conTraza.trazaSintacticaTruncada());
        int multiplicacion = indiceNodo(conTraza.trazaSintactica(), "*");
        int suma = indiceNodo(conTraza.trazaSintactica(), "+");
        assertTrue(multiplicacion < suma);
        assertMismoResultadoCompilacion(sinTraza, conTraza);
    }

    @Test
    void conservarTrazaHastaElErrorYLaRecuperacion() {
        CompilerService.ResultadoCompilacion resultado = service.compilar("int x; x = 10 + ;", true);

        assertFalse(resultado.exitoso());
        assertFalse(resultado.erroresSintacticos().isEmpty());
        assertTrue(resultado.trazaSintactica().stream().anyMatch(p -> p.evento() == ERROR_SINTACTICO));
        assertTrue(resultado.trazaSintactica().stream().anyMatch(p -> p.evento() == INICIAR_RECUPERACION));
        assertTrue(resultado.trazaSintactica().stream().anyMatch(p -> p.evento() == FINALIZAR_RECUPERACION));
    }

    @Test
    void noInventarTrazaCuandoElLexerImpideEjecutarElParser() {
        CompilerService.ResultadoCompilacion resultado = service.compilar("int x; x = 1 @ 2;", true);

        assertFalse(resultado.erroresLexicos().isEmpty());
        assertNotNull(resultado.trazaSintactica());
        assertTrue(resultado.trazaSintactica().isEmpty());
        assertFalse(resultado.trazaSintacticaTruncada());
        assertNull(resultado.ast());
    }

    @Test
    void devolverTrazaVaciaParaEntradaVaciaPorqueElParserNoSeEjecuta() {
        CompilerService.ResultadoCompilacion resultado = service.compilar(" ", true);

        assertTrue(resultado.trazaSintactica().isEmpty());
        assertFalse(resultado.trazaSintacticaTruncada());
    }

    @Test
    void conservarTrazaCuandoElProgramaFallaEnLaFaseSemantica() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = y + 1;", true);

        assertEquals(CompilerService.EstadoCompilacion.ERROR_SEMANTICO, resultado.estado());
        assertFalse(resultado.trazaSintactica().isEmpty());
        assertTrue(resultado.erroresSintacticos().isEmpty());
        assertEquals(List.of("Variable no declarada: 'y'"), resultado.erroresSemanticos());
    }

    @Test
    void informarTruncamientoSinInterrumpirLaCompilacion() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            codigo.append("int x").append(i).append(";\n");
        }

        CompilerService.ResultadoCompilacion resultado = service.compilar(codigo.toString(), true);

        assertTrue(resultado.exitoso());
        assertEquals(1000, resultado.trazaSintactica().size());
        assertTrue(resultado.trazaSintacticaTruncada());
        assertEquals(100, resultado.simbolos().size());
    }

    @Test
    void aislarTrazasEntreSolicitudesConcurrentes() throws Exception {
        ExecutorService ejecutor = Executors.newFixedThreadPool(6);
        try {
            List<Callable<CompilerService.ResultadoCompilacion>> tareas = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                int numero = i;
                tareas.add(() -> service.compilar("int x" + numero + ";", numero % 2 == 0));
            }

            List<Future<CompilerService.ResultadoCompilacion>> resultados = ejecutor.invokeAll(tareas);
            for (int i = 0; i < resultados.size(); i++) {
                int numero = i;
                CompilerService.ResultadoCompilacion resultado = resultados.get(i).get();
                assertTrue(resultado.exitoso());
                if (i % 2 == 0) {
                    assertFalse(resultado.trazaSintactica().isEmpty());
                    assertTrue(resultado.trazaSintactica().stream()
                            .anyMatch(p -> ("x" + numero).equals(p.tokenActual())));
                } else {
                    assertNull(resultado.trazaSintactica());
                    assertNull(resultado.trazaSintacticaTruncada());
                }
            }
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private int indiceNodo(List<PasoSintactico> pasos, String operador) {
        for (int i = 0; i < pasos.size(); i++) {
            PasoSintactico paso = pasos.get(i);
            if (paso.evento() == CONSTRUIR_NODO && "OPERADOR".equals(paso.nodoTipo())
                    && operador.equals(paso.nodoValor())) {
                return i;
            }
        }
        throw new AssertionError("No se encontro el operador " + operador);
    }

    private void assertMismoResultadoCompilacion(
            CompilerService.ResultadoCompilacion esperado,
            CompilerService.ResultadoCompilacion actual) {
        assertEquals(esperado.exitoso(), actual.exitoso());
        assertEquals(esperado.mensaje(), actual.mensaje());
        assertEquals(esperado.codigoFuente(), actual.codigoFuente());
        assertEquals(esperado.tokens(), actual.tokens());
        assertEquals(esperado.ast().toString(), actual.ast().toString());
        assertEquals(esperado.erroresLexicos(), actual.erroresLexicos());
        assertEquals(esperado.erroresSintacticos(), actual.erroresSintacticos());
        assertEquals(esperado.erroresSemanticos(), actual.erroresSemanticos());
        assertEquals(esperado.simbolos(), actual.simbolos());
        assertEquals(esperado.codigoIntermedio(), actual.codigoIntermedio());
        assertEquals(esperado.neander(), actual.neander());
        assertEquals(esperado.estado(), actual.estado());
    }
}

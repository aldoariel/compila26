package edu.fpune.compila26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CompilerServiceTest {

    private final CompilerService service = new CompilerService();

    @Test
    void compilarTodasLasFasesParaCodigoValido() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = 10 + 5 * 2;");

        assertTrue(resultado.exitoso());
        assertEquals("Compilación completada correctamente.", resultado.mensaje());
        assertEquals(11, resultado.tokens().size());
        assertNotNull(resultado.ast());
        assertEquals(List.of(new CompilerService.SimboloCompilacion("x", "int")),
                resultado.simbolos());
        assertEquals(List.of(
                "t0 = 5 * 2",
                "t1 = 10 + t0",
                "x = t1"
        ), resultado.codigoIntermedio());
        assertTrue(resultado.erroresLexicos().isEmpty());
        assertTrue(resultado.erroresSintacticos().isEmpty());
        assertTrue(resultado.erroresSemanticos().isEmpty());
    }

    @Test
    void detenerDespuesDelLexerAnteCaracterInvalido() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("x = 10 @ 2;");

        assertFalse(resultado.exitoso());
        assertEquals(6, resultado.tokens().size());
        assertEquals(List.of("Error léxico: carácter no reconocido '@' en línea 1, columna 8."),
                resultado.erroresLexicos());
        assertNull(resultado.ast());
        assertTrue(resultado.erroresSintacticos().isEmpty());
        assertTrue(resultado.erroresSemanticos().isEmpty());
        assertTrue(resultado.simbolos().isEmpty());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void detenerDespuesDelParserYConservarAstParcial() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("x = 10 + ;");

        assertFalse(resultado.exitoso());
        assertFalse(resultado.tokens().isEmpty());
        assertNotNull(resultado.ast());
        assertFalse(resultado.erroresSintacticos().isEmpty());
        assertTrue(resultado.erroresLexicos().isEmpty());
        assertTrue(resultado.erroresSemanticos().isEmpty());
        assertTrue(resultado.simbolos().isEmpty());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void detenerDespuesDelSemanticoYConservarResultadosPrevios() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = y + 10;");

        assertFalse(resultado.exitoso());
        assertFalse(resultado.tokens().isEmpty());
        assertNotNull(resultado.ast());
        assertEquals(List.of("Variable no declarada: 'y'"), resultado.erroresSemanticos());
        assertTrue(resultado.erroresLexicos().isEmpty());
        assertTrue(resultado.erroresSintacticos().isEmpty());
        assertEquals(List.of(new CompilerService.SimboloCompilacion("x", "int")),
                resultado.simbolos());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void compilarMultiplesDeclaracionesYAsignaciones() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; int y; x = 10; y = x + 5;");

        assertTrue(resultado.exitoso());
        assertEquals(List.of(
                new CompilerService.SimboloCompilacion("x", "int"),
                new CompilerService.SimboloCompilacion("y", "int")
        ), resultado.simbolos());
        assertEquals(List.of("x = 10", "t0 = x + 5", "y = t0"),
                resultado.codigoIntermedio());
    }

    @Test
    void detenerPipelineAnteRedeclaracion() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; int x;");

        assertFalse(resultado.exitoso());
        assertEquals(List.of("Variable 'x' ya declarada."), resultado.erroresSemanticos());
        assertEquals(List.of(new CompilerService.SimboloCompilacion("x", "int")),
                resultado.simbolos());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void conservarAgrupacionPorParentesisConDeclaracionExplicita() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = (10 + 5) * 2;");

        assertTrue(resultado.exitoso());
        assertEquals(List.of(
                "t0 = 10 + 5",
                "t1 = t0 * 2",
                "x = t1"
        ), resultado.codigoIntermedio());
    }

    @Test
    void rechazarCodigoVacioSinEjecutarFases() {
        CompilerService.ResultadoCompilacion resultado = service.compilar(" ");

        assertFalse(resultado.exitoso());
        assertEquals("El código fuente está vacío.", resultado.mensaje());
        assertTrue(resultado.tokens().isEmpty());
        assertNull(resultado.ast());
        assertTrue(resultado.erroresLexicos().isEmpty());
        assertTrue(resultado.erroresSintacticos().isEmpty());
        assertTrue(resultado.erroresSemanticos().isEmpty());
        assertTrue(resultado.simbolos().isEmpty());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void generarNeanderDespuesDelCodigoIntermedioValido() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = 10 + 5;");

        assertTrue(resultado.exitoso());
        assertTrue(resultado.neander().ejecutado());
        assertTrue(resultado.neander().exitoso());
        assertEquals(List.of(
                "LDA 82H", "ADD 83H", "STA 81H",
                "LDA 81H", "STA 80H", "HLT"
        ), resultado.neander().instrucciones());
        assertFalse(resultado.neander().datos().isEmpty());
        assertEquals("VARIABLE", resultado.neander().datos().get(0).tipo());
        assertEquals("x", resultado.neander().datos().get(0).nombre());
        assertEquals("80H", resultado.neander().datos().get(0).direccion());
    }

    @Test
    void mostrarExpansionNeanderDeLaResta() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = 10 - 5;");

        assertTrue(resultado.neander().exitoso());
        assertEquals(List.of(
                "LDA 83H", "NOT", "ADD 84H", "STA 85H",
                "LDA 82H", "ADD 85H", "STA 81H",
                "LDA 81H", "STA 80H", "HLT"
        ), resultado.neander().instrucciones());
        assertTrue(resultado.neander().datos().stream()
                .anyMatch(dato -> "TEMPORAL_INTERNO".equals(dato.tipo())));
    }

    @Test
    void distinguirLimitacionNeanderDeErrorDelPrograma() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = 10 * 5;");

        assertTrue(resultado.exitoso());
        assertEquals(List.of("t0 = 10 * 5", "x = t0"), resultado.codigoIntermedio());
        assertTrue(resultado.neander().ejecutado());
        assertFalse(resultado.neander().exitoso());
        assertTrue(resultado.neander().instrucciones().isEmpty());
        assertTrue(resultado.neander().diagnosticos().get(0).contains("MULTIPLICAR"));
    }

    @Test
    void noEjecutarNeanderCuandoFallaUnaFaseAnterior() {
        CompilerService.ResultadoCompilacion resultado =
                service.compilar("int x; x = y + 10;");

        assertFalse(resultado.exitoso());
        assertFalse(resultado.neander().ejecutado());
        assertFalse(resultado.neander().exitoso());
        assertTrue(resultado.neander().instrucciones().isEmpty());
        assertTrue(resultado.neander().datos().isEmpty());
        assertTrue(resultado.neander().diagnosticos().isEmpty());
    }
}

package edu.fpune.compila26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class IntermedioServiceTest {

    private final IntermedioService service = new IntermedioService();

    @Test
    void generarCodigoIntermedioParaProgramaValido() {
        IntermedioService.ResultadoIntermedio resultado =
                service.generar("int x; x = 10 + 5 * 2;");

        assertTrue(resultado.exitoso());
        assertEquals("Código intermedio generado correctamente.", resultado.mensaje());
        assertEquals(List.of(
                "t0 = 5 * 2",
                "t1 = 10 + t0",
                "x = t1"
        ), resultado.codigoIntermedio());
        assertTrue(resultado.errores().isEmpty());
    }

    @Test
    void detenerPipelineAnteErrorLexico() {
        IntermedioService.ResultadoIntermedio resultado =
                service.generar("x = 10 @ 2;");

        assertFalse(resultado.exitoso());
        assertEquals("Se encontraron errores léxicos.", resultado.mensaje());
        assertEquals(List.of("Error léxico: carácter no reconocido '@' en línea 1, columna 8."),
                resultado.errores());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void detenerPipelineAnteErrorSintactico() {
        IntermedioService.ResultadoIntermedio resultado =
                service.generar("x = 10 + ;");

        assertFalse(resultado.exitoso());
        assertEquals("Se encontraron errores sintácticos.", resultado.mensaje());
        assertFalse(resultado.errores().isEmpty());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void detenerPipelineAnteVariableNoDeclarada() {
        IntermedioService.ResultadoIntermedio resultado =
                service.generar("int x; x = y + 10;");

        assertFalse(resultado.exitoso());
        assertEquals("Se encontraron errores semánticos.", resultado.mensaje());
        assertEquals(List.of("Variable no declarada: 'y'"), resultado.errores());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }

    @Test
    void rechazarCodigoVacio() {
        IntermedioService.ResultadoIntermedio resultado = service.generar(" ");

        assertFalse(resultado.exitoso());
        assertEquals("El código fuente está vacío.", resultado.mensaje());
        assertEquals(List.of("El código fuente está vacío."), resultado.errores());
        assertTrue(resultado.codigoIntermedio().isEmpty());
    }
}

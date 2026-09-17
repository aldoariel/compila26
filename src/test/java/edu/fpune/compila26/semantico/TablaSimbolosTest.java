package edu.fpune.compila26.semantico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class TablaSimbolosTest {

    @Test
    void declararYConsultarSimbolo() {
        TablaSimbolos tabla = new TablaSimbolos();

        assertTrue(tabla.declarar("x", "int"));
        assertTrue(tabla.estaDeclarado("x"));
    }

    @Test
    void obtenerSimboloDeclarado() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("x", "int");

        Simbolo simbolo = tabla.obtener("x");

        assertNotNull(simbolo);
        assertEquals("x", simbolo.getNombre());
        assertEquals("int", simbolo.getTipo());
    }

    @Test
    void obtenerTipoDelSimbolo() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("x", "int");

        assertEquals("int", tabla.obtenerTipo("x"));
    }

    @Test
    void rechazarDeclaracionDuplicadaYConservarPrimerTipo() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("x", "int");

        assertFalse(tabla.declarar("x", "otro"));
        assertEquals("int", tabla.obtenerTipo("x"));
        assertEquals(1, tabla.cantidad());
    }

    @Test
    void informarCantidadDeSimbolos() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("x", "int");
        tabla.declarar("y", "int");

        assertEquals(2, tabla.cantidad());
    }

    @Test
    void conservarOrdenDeInsercion() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("z", "int");
        tabla.declarar("x", "int");
        tabla.declarar("y", "int");

        assertEquals(List.of("z", "x", "y"),
                tabla.getSimbolos().stream().map(Simbolo::getNombre).toList());
    }

    @Test
    void limpiarTodosLosSimbolos() {
        TablaSimbolos tabla = new TablaSimbolos();
        tabla.declarar("x", "int");

        tabla.limpiar();

        assertEquals(0, tabla.cantidad());
        assertFalse(tabla.estaDeclarado("x"));
        assertTrue(tabla.getSimbolos().isEmpty());
    }

    @Test
    void devolverNullParaSimboloInexistente() {
        TablaSimbolos tabla = new TablaSimbolos();

        assertNull(tabla.obtener("inexistente"));
        assertNull(tabla.obtenerTipo("inexistente"));
        assertFalse(tabla.estaDeclarado("inexistente"));
    }
}

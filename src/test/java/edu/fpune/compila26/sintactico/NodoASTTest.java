package edu.fpune.compila26.sintactico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class NodoASTTest {

    @Test
    void construirNodoConTipoValorEHijosVacios() {
        NodoAST nodo = new NodoAST("NUMERO", "10");

        assertEquals("NUMERO", nodo.getTipo());
        assertEquals("10", nodo.getValor());
        assertTrue(nodo.getHijos().isEmpty());
    }

    @Test
    void conservarOrdenDeLosHijosEIgnorarHijoNulo() {
        NodoAST padre = new NodoAST("OPERADOR", "+");
        NodoAST izquierdo = new NodoAST("NUMERO", "1");
        NodoAST derecho = new NodoAST("NUMERO", "2");

        padre.agregarHijo(izquierdo);
        padre.agregarHijo(null);
        padre.agregarHijo(derecho);

        assertEquals(List.of(izquierdo, derecho), padre.getHijos());
    }

    @Test
    void representarArbolComoTextoIndentado() {
        NodoAST programa = new NodoAST("PROGRAMA");
        NodoAST numero = new NodoAST("NUMERO", "10");
        programa.agregarHijo(numero);

        assertEquals("PROGRAMA\n  NUMERO: 10\n", programa.toString());
    }

    @Test
    void permitirMutacionYReemplazarListaNulaPorListaVacia() {
        NodoAST nodo = new NodoAST();
        assertNull(nodo.getTipo());
        assertNull(nodo.getValor());

        nodo.setTipo("ID");
        nodo.setValor("x");
        nodo.getHijos().add(new NodoAST("NUMERO", "1"));
        nodo.setHijos(null);

        assertEquals("ID", nodo.getTipo());
        assertEquals("x", nodo.getValor());
        assertTrue(nodo.getHijos().isEmpty());
    }
}

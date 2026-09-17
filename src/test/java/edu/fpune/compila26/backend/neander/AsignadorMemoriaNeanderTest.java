package edu.fpune.compila26.backend.neander;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.intermedio.Operando;

class AsignadorMemoriaNeanderTest {

    @Test
    void asignarPrimeraDireccionDesde80Hexadecimal() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();

        int direccion = asignador.asignar(Operando.variable("x"));

        assertEquals(0x80, direccion);
        assertEquals("80H", asignador.getDatos().get(0).direccionHexadecimal());
    }

    @Test
    void conservarDireccionParaLaMismaVariable() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();
        Operando variable = Operando.variable("x");

        assertEquals(asignador.asignar(variable), asignador.asignar(variable));
        assertEquals(1, asignador.getDatos().size());
    }

    @Test
    void asignarDireccionesDistintasAVariablesDistintas() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();

        assertEquals(0x80, asignador.asignar(Operando.variable("x")));
        assertEquals(0x81, asignador.asignar(Operando.variable("y")));
    }

    @Test
    void asignarMemoriaATemporales() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();

        assertEquals(0x80, asignador.asignar(Operando.temporal("t0")));
        assertNull(asignador.getDatos().get(0).valorInicial());
    }

    @Test
    void representarConstanteComoDatoDeOchoBits() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();
        Operando constante = Operando.constante("-5");

        asignador.asignar(constante);

        DatoNeander dato = asignador.getDatos().get(0);
        assertEquals(constante, dato.operando());
        assertEquals(0xFB, dato.valorInicial());
    }

    @Test
    void aceptarLimitesDeConstantesConSignoDeOchoBits() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();

        asignador.asignar(Operando.constante("-128"));
        asignador.asignar(Operando.constante("127"));

        assertEquals(0x80, asignador.getDatos().get(0).valorInicial());
        assertEquals(0x7F, asignador.getDatos().get(1).valorInicial());
    }

    @Test
    void rechazarConstantesFueraDelRangoConSignoDeOchoBits() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();

        assertThrows(MemoriaNeanderException.class,
                () -> asignador.asignar(Operando.constante("-129")));
        assertThrows(MemoriaNeanderException.class,
                () -> asignador.asignar(Operando.constante("128")));
    }

    @Test
    void informarAgotamientoDespuesDeFFHexadecimal() {
        AsignadorMemoriaNeander asignador = new AsignadorMemoriaNeander();
        for (int indice = 0; indice < 128; indice++) {
            asignador.asignar(Operando.variable("v" + indice));
        }

        MemoriaNeanderException error = assertThrows(
                MemoriaNeanderException.class,
                () -> asignador.asignar(Operando.variable("sinEspacio"))
        );

        assertEquals("Memoria de datos NEANDER agotada.", error.getMessage());
    }
}

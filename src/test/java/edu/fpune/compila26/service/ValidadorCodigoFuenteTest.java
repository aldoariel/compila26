package edu.fpune.compila26.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ValidadorCodigoFuenteTest {

    @Test
    void permiteCodigoEnElLimiteIncluyendoUnicodeYVariasLineas() {
        ValidadorCodigoFuente validador = new ValidadorCodigoFuente(5);

        assertDoesNotThrow(() -> validador.validarTamanio("a\n😀bc"));
    }

    @Test
    void rechazaPorCantidadDeCaracteresUnicodeNoPorUnidadesUtf16() {
        ValidadorCodigoFuente validador = new ValidadorCodigoFuente(3);

        CodigoFuenteDemasiadoGrandeException error = assertThrows(
                CodigoFuenteDemasiadoGrandeException.class,
                () -> validador.validarTamanio("😀abc")
        );

        assertEquals(3, error.getMaximoCaracteres());
        assertEquals(4, error.getCaracteresRecibidos());
    }

    @Test
    void permiteEntradaNulaParaQueElServicioConserveSuContrato() {
        ValidadorCodigoFuente validador = new ValidadorCodigoFuente(1);

        assertDoesNotThrow(() -> validador.validarTamanio(null));
    }
}

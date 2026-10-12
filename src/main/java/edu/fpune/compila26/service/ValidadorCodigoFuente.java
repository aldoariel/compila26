package edu.fpune.compila26.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCodigoFuente {

    public static final int MAXIMO_CARACTERES_PREDETERMINADO = 20_000;

    private final int maximoCaracteres;

    public ValidadorCodigoFuente() {
        this(MAXIMO_CARACTERES_PREDETERMINADO);
    }

    @Autowired
    public ValidadorCodigoFuente(
            @Value("${compila26.codigo.max-caracteres:20000}") int maximoCaracteres) {
        if (maximoCaracteres <= 0) {
            throw new IllegalArgumentException("El l\u00edmite de c\u00f3digo fuente debe ser positivo.");
        }
        this.maximoCaracteres = maximoCaracteres;
    }

    public void validarTamanio(String codigoFuente) {
        if (codigoFuente == null) {
            return;
        }

        int cantidadCaracteres = codigoFuente.codePointCount(0, codigoFuente.length());
        if (cantidadCaracteres > maximoCaracteres) {
            throw new CodigoFuenteDemasiadoGrandeException(maximoCaracteres, cantidadCaracteres);
        }
    }
}

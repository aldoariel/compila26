package edu.fpune.compila26.service;

public class CodigoFuenteDemasiadoGrandeException extends RuntimeException {

    private final int maximoCaracteres;
    private final int caracteresRecibidos;

    public CodigoFuenteDemasiadoGrandeException(int maximoCaracteres, int caracteresRecibidos) {
        super("El c\u00f3digo fuente supera el l\u00edmite de " + maximoCaracteres + " caracteres.");
        this.maximoCaracteres = maximoCaracteres;
        this.caracteresRecibidos = caracteresRecibidos;
    }

    public int getMaximoCaracteres() {
        return maximoCaracteres;
    }

    public int getCaracteresRecibidos() {
        return caracteresRecibidos;
    }
}

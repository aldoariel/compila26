package edu.fpune.compila26.intermedio;

public enum OperacionIntermedia {
    ASIGNAR(null),
    SUMAR("+"),
    RESTAR("-"),
    MULTIPLICAR("*"),
    DIVIDIR("/");

    private final String simbolo;

    OperacionIntermedia(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public static OperacionIntermedia desdeSimbolo(String simbolo) {
        return switch (simbolo) {
            case "+" -> SUMAR;
            case "-" -> RESTAR;
            case "*" -> MULTIPLICAR;
            case "/" -> DIVIDIR;
            default -> null;
        };
    }
}

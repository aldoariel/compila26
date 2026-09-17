package edu.fpune.compila26.intermedio;

public record Operando(
        TipoOperando tipo,
        String valor
) {
    public static Operando constante(String valor) {
        return new Operando(TipoOperando.CONSTANTE, valor);
    }

    public static Operando variable(String nombre) {
        return new Operando(TipoOperando.VARIABLE, nombre);
    }

    public static Operando temporal(String nombre) {
        return new Operando(TipoOperando.TEMPORAL, nombre);
    }

    @Override
    public String toString() {
        return valor;
    }
}

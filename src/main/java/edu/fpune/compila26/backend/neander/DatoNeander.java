package edu.fpune.compila26.backend.neander;

import edu.fpune.compila26.intermedio.Operando;

public record DatoNeander(
        Operando operando,
        int direccion,
        Integer valorInicial,
        boolean temporalInterno
) {
    public DatoNeander(Operando operando, int direccion, Integer valorInicial) {
        this(operando, direccion, valorInicial, false);
    }

    public String direccionHexadecimal() {
        return String.format("%02XH", direccion);
    }
}

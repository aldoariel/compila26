package edu.fpune.compila26.intermedio;

public record InstruccionIntermedia(
        OperacionIntermedia operacion,
        Operando destino,
        Operando operando1,
        Operando operando2
) {
    public static InstruccionIntermedia asignar(Operando destino, Operando origen) {
        return new InstruccionIntermedia(
                OperacionIntermedia.ASIGNAR,
                destino,
                origen,
                null
        );
    }

    public String formatear() {
        if (operacion == OperacionIntermedia.ASIGNAR) {
            return destino + " = " + operando1;
        }

        return destino
                + " = "
                + operando1
                + " "
                + operacion.getSimbolo()
                + " "
                + operando2;
    }

    @Override
    public String toString() {
        return formatear();
    }
}

package edu.fpune.compila26.backend.neander;

public record InstruccionEnsambladorNeander(
        InstruccionNeander instruccion,
        Integer direccion
) {
    public InstruccionEnsambladorNeander {
        if (instruccion == null) {
            throw new IllegalArgumentException("La instrucción NEANDER es obligatoria.");
        }
        if (instruccion.requiereDireccion() && direccion == null) {
            throw new IllegalArgumentException("La instrucción requiere una dirección.");
        }
        if (!instruccion.requiereDireccion() && direccion != null) {
            throw new IllegalArgumentException("La instrucción no admite dirección.");
        }
        if (direccion != null && (direccion < 0 || direccion > 0xFF)) {
            throw new IllegalArgumentException("La dirección debe estar entre 00H y FFH.");
        }
    }

    public static InstruccionEnsambladorNeander conDireccion(
            InstruccionNeander instruccion,
            int direccion) {
        return new InstruccionEnsambladorNeander(instruccion, direccion);
    }

    public static InstruccionEnsambladorNeander sinDireccion(InstruccionNeander instruccion) {
        return new InstruccionEnsambladorNeander(instruccion, null);
    }

    public String formatear() {
        if (direccion == null) {
            return instruccion.name();
        }
        return instruccion.name() + " " + String.format("%02XH", direccion);
    }

    @Override
    public String toString() {
        return formatear();
    }
}

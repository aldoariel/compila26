package edu.fpune.compila26.backend.neander;

public enum InstruccionNeander {
    NOP(false),
    STA(true),
    LDA(true),
    ADD(true),
    OR(true),
    AND(true),
    NOT(false),
    JMP(true),
    JN(true),
    JZ(true),
    HLT(false);

    private final boolean requiereDireccion;

    InstruccionNeander(boolean requiereDireccion) {
        this.requiereDireccion = requiereDireccion;
    }

    public boolean requiereDireccion() {
        return requiereDireccion;
    }
}

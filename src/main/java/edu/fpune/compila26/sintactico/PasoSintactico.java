package edu.fpune.compila26.sintactico;

/** Evento observable producido por el parser durante el analisis sintactico. */
public record PasoSintactico(
        int numero,
        Evento evento,
        String funcion,
        String produccion,
        String tokenActual,
        String tipoToken,
        Integer linea,
        Integer columna,
        int profundidad,
        String descripcion,
        String nodoTipo,
        String nodoValor) {

    public enum Evento {
        ENTRAR_FUNCION, SALIR_FUNCION, ANTICIPACION, SELECCIONAR_PRODUCCION,
        CONSUMIR_TOKEN, CONSTRUIR_NODO, ERROR_SINTACTICO,
        INICIAR_RECUPERACION, DESCARTAR_TOKEN, FINALIZAR_RECUPERACION
    }
}

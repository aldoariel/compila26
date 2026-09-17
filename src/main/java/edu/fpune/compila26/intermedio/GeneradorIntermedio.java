package edu.fpune.compila26.intermedio;

import java.util.ArrayList;
import java.util.List;

import edu.fpune.compila26.sintactico.NodoAST;

public class GeneradorIntermedio {

    private final List<InstruccionIntermedia> instrucciones;
    private final List<String> codigoIntermedio;
    private int contadorTemporales;

    public GeneradorIntermedio() {
        this.instrucciones = new ArrayList<>();
        this.codigoIntermedio = new ArrayList<>();
        this.contadorTemporales = 0;
    }

    /**
     * Genera código intermedio a partir del AST.
     *
     * Ejemplo:
     *
     * x = 10 + 5 * 2;
     *
     * genera:
     *
     * t0 = 5 * 2
     * t1 = 10 + t0
     * x = t1
     *
     * @param ast árbol sintáctico
     * @return lista de instrucciones de código intermedio
     */
    public List<String> generar(NodoAST ast) {
        generarInstrucciones(ast);

        if (ast == null) {
            return codigoIntermedio;
        }

        return new ArrayList<>(codigoIntermedio);
    }

    public List<InstruccionIntermedia> generarInstrucciones(NodoAST ast) {
        limpiar();

        if (ast != null) {
            generarNodo(ast);
        }

        instrucciones.stream()
                .map(InstruccionIntermedia::formatear)
                .forEach(codigoIntermedio::add);

        return new ArrayList<>(instrucciones);
    }

    /**
     * Procesa recursivamente los nodos del AST.
     *
     * @return el nombre del temporal, identificador
     *         o valor producido por el nodo.
     */
    private Operando generarNodo(NodoAST nodo) {

        if (nodo == null) {
            return Operando.variable("");
        }

        String tipo = nodo.getTipo();

        switch (tipo) {

            case "PROGRAMA":

                return generarPrograma(nodo);

            case "ASIGNACION":

                return generarAsignacion(nodo);

            case "DECLARACION":

                return Operando.variable("");

            case "OPERADOR":

                return generarOperador(nodo);

            case "ID":

                return Operando.variable(nodo.getValor());

            case "NUMERO":

                return Operando.constante(nodo.getValor());

            default:

                return Operando.variable("");
        }
    }

    /**
     * Genera código para el nodo PROGRAMA.
     *
     * PROGRAMA
     *   ASIGNACION
     *   ASIGNACION
     */
    private Operando generarPrograma(NodoAST nodo) {

        for (NodoAST hijo : nodo.getHijos()) {
            generarNodo(hijo);
        }

        return Operando.variable("");
    }

    /**
     * Genera código para una asignación.
     *
     * Ejemplo:
     *
     * ASIGNACION
     *   ID (x)
     *   NUMERO (10)
     *
     * Resultado:
     *
     * x = 10
     */
    private Operando generarAsignacion(NodoAST nodo) {

        if (nodo.getHijos().size() < 2) {
            return Operando.variable("");
        }

        NodoAST nodoId = nodo.getHijos().get(0);
        NodoAST expresion = nodo.getHijos().get(1);

        Operando variable = Operando.variable(nodoId.getValor());

        Operando resultadoExpresion =
                generarNodo(expresion);

        instrucciones.add(InstruccionIntermedia.asignar(variable, resultadoExpresion));

        return variable;
    }

    /**
     * Genera código para una operación.
     *
     * Ejemplo:
     *
     * OPERADOR (+)
     *   NUMERO (10)
     *   NUMERO (5)
     *
     * Resultado:
     *
     * t0 = 10 + 5
     */
    private Operando generarOperador(NodoAST nodo) {

        if (nodo.getHijos().size() < 2) {
            return Operando.variable("");
        }

        OperacionIntermedia operacion =
                OperacionIntermedia.desdeSimbolo(nodo.getValor());

        if (operacion == null) {
            return Operando.variable("");
        }

        NodoAST izquierdo =
                nodo.getHijos().get(0);

        NodoAST derecho =
                nodo.getHijos().get(1);

        Operando valorIzquierdo =
                generarNodo(izquierdo);

        Operando valorDerecho =
                generarNodo(derecho);

        Operando temporal =
                nuevoTemporal();

        instrucciones.add(new InstruccionIntermedia(
                operacion,
                temporal,
                valorIzquierdo,
                valorDerecho
        ));

        return temporal;
    }

    /**
     * Genera un nuevo temporal.
     *
     * t0
     * t1
     * t2
     * ...
     */
    private Operando nuevoTemporal() {

        return Operando.temporal("t" + contadorTemporales++);
    }

    public List<InstruccionIntermedia> getInstrucciones() {
        return new ArrayList<>(instrucciones);
    }

    /**
     * Devuelve el código intermedio generado.
     */
    public List<String> getCodigoIntermedio() {

        return new ArrayList<>(codigoIntermedio);
    }

    /**
     * Reinicia el generador.
     */
    public void limpiar() {

        instrucciones.clear();
        codigoIntermedio.clear();
        contadorTemporales = 0;
    }
}

package edu.fpune.compila26.semantico;

import java.util.ArrayList;
import java.util.List;

import edu.fpune.compila26.sintactico.NodoAST;

public class AnalizadorSemantico {

    private final TablaSimbolos tabla;
    private final List<String> errores;

    public AnalizadorSemantico() {
        this.tabla = new TablaSimbolos();
        this.errores = new ArrayList<>();
    }

    /**
     * Analiza el AST completo.
     *
     * @param ast árbol sintáctico generado por el Parser
     * @return true si no existen errores semánticos
     */
    public boolean analizar(NodoAST ast) {

        if (ast == null) {
            errores.add("El AST es nulo.");
            return false;
        }

        analizarNodo(ast);

        return errores.isEmpty();
    }

    /**
     * Analiza cada tipo de nodo del AST.
     */
    private String analizarNodo(NodoAST nodo) {

        if (nodo == null) {
            return "desconocido";
        }

        String tipo = nodo.getTipo();

        switch (tipo) {

            case "PROGRAMA":

                for (NodoAST hijo : nodo.getHijos()) {
                    analizarNodo(hijo);
                }

                return "void";

            case "ASIGNACION":

                return analizarAsignacion(nodo);

            case "DECLARACION":

                return analizarDeclaracion(nodo);

            case "ID":

                return analizarIdentificador(nodo);

            case "NUMERO":

                return "int";

            case "OPERADOR":

                return analizarOperador(nodo);

            default:

                errores.add(
                        "Nodo semántico no reconocido: " + tipo
                );

                return "desconocido";
        }
    }

    /**
     * Analiza una asignación.
     *
     * Ejemplo:
     *
     * ASIGNACION
     *   ID (x)
     *   NUMERO (10)
     *
     * La variable debe haber sido declarada previamente.
     */
    private String analizarAsignacion(NodoAST nodo) {

        if (nodo.getHijos().size() < 2) {

            errores.add(
                    "Asignación incompleta."
            );

            return "desconocido";
        }

        NodoAST nodoId = nodo.getHijos().get(0);
        NodoAST expresion = nodo.getHijos().get(1);

        String nombreVariable = nodoId.getValor();

        boolean variableDeclarada = tabla.estaDeclarado(nombreVariable);

        if (!variableDeclarada) {
            errores.add(
                    "Variable no declarada: '"
                            + nombreVariable
                            + "'"
            );
        }

        // Primero analizamos la expresión.
        String tipoExpresion = analizarNodo(expresion);

        if (!variableDeclarada) {
            return "desconocido";
        }

        /*
         * La variable ya existe.
         */
        String tipoVariable =
                tabla.obtenerTipo(nombreVariable);

        /*
         * Solo verificamos incompatibilidad si la expresión
         * pudo determinarse correctamente.
         *
         * Esto evita errores en cascada.
         */
        if (!tipoExpresion.equals("desconocido")
                && !tipoVariable.equals(tipoExpresion)) {

            errores.add(
                    "Incompatibilidad de tipos: la variable '"
                            + nombreVariable
                            + "' es de tipo "
                            + tipoVariable
                            + " y se intenta asignar un valor de tipo "
                            + tipoExpresion
            );
        }

        return tipoVariable;
    }

    private String analizarDeclaracion(NodoAST nodo) {
        if (nodo.getHijos().isEmpty()) {
            errores.add("Declaración incompleta.");
            return "desconocido";
        }

        String tipo = nodo.getValor();
        String nombreVariable = nodo.getHijos().get(0).getValor();

        if (!tabla.declarar(nombreVariable, tipo)) {
            errores.add("Variable '" + nombreVariable + "' ya declarada.");
            return "desconocido";
        }

        return tipo;
    }

    /**
     * Analiza el uso de una variable.
     *
     * Ejemplo:
     *
     * x = y + 10;
     *
     * Si y no fue declarada anteriormente,
     * se genera un error semántico.
     */
    private String analizarIdentificador(NodoAST nodo) {

        String nombre = nodo.getValor();

        if (!tabla.estaDeclarado(nombre)) {

            errores.add(
                    "Variable no declarada: '"
                            + nombre
                            + "'"
            );

            return "desconocido";
        }

        return tabla.obtenerTipo(nombre);
    }

    /**
     * Analiza operadores aritméticos:
     *
     * + - * /
     *
     * Por ahora todos requieren operandos de tipo int
     * y producen un resultado de tipo int.
     */
    private String analizarOperador(NodoAST nodo) {

        String operador = nodo.getValor();

        /*
         * Un operador binario necesita dos operandos.
         */
        if (nodo.getHijos().size() < 2) {

            errores.add(
                    "Operador '"
                            + operador
                            + "' incompleto."
            );

            return "desconocido";
        }

        /*
         * Analizamos ambos operandos.
         */
        String tipoIzquierdo =
                analizarNodo(
                        nodo.getHijos().get(0)
                );

        String tipoDerecho =
                analizarNodo(
                        nodo.getHijos().get(1)
                );

        /*
         * Si alguno de los operandos tiene un error
         * previo, no generamos otro error.
         *
         * Esto evita errores en cascada.
         *
         * Ejemplo:
         *
         * x = y + 10;
         *
         * El único error real es:
         *
         * Variable no declarada: 'y'
         */
        if (tipoIzquierdo.equals("desconocido")
                || tipoDerecho.equals("desconocido")) {

            return "desconocido";
        }

        /*
         * Los operadores aritméticos requieren
         * operandos de tipo int.
         */
        if (!tipoIzquierdo.equals("int")
                || !tipoDerecho.equals("int")) {

            errores.add(
                    "El operador '"
                            + operador
                            + "' requiere operandos de tipo int."
            );

            return "desconocido";
        }

        /*
         * El resultado de una operación aritmética
         * entre enteros es entero.
         */
        return "int";
    }

    /**
     * Devuelve la tabla de símbolos.
     */
    public TablaSimbolos getTabla() {
        return tabla;
    }

    /**
     * Devuelve la lista de errores semánticos.
     */
    public List<String> getErrores() {
        return errores;
    }
}

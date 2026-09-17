package edu.fpune.compila26.sintactico;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del Árbol de Sintaxis Abstracta (AST).
 *
 * Representa un elemento de la estructura sintáctica
 * del programa analizado por el Parser.
 */
public class NodoAST {

    /**
     * Tipo de nodo.
     *
     * Ejemplos:
     * PROGRAMA
     * ASIGNACION
     * SUMA
     * RESTA
     * MULTIPLICACION
     * DIVISION
     * NUMERO
     * IDENTIFICADOR
     */
    private String tipo;

    /**
     * Valor asociado al nodo.
     *
     * Por ejemplo:
     * - Para NUMERO: "10"
     * - Para IDENTIFICADOR: "x"
     * - Para operadores: "+"
     */
    private String valor;

    /**
     * Hijos del nodo.
     */
    private List<NodoAST> hijos;

    /**
     * Constructor vacío.
     */
    public NodoAST() {
        this.hijos = new ArrayList<>();
    }

    /**
     * Constructor con tipo.
     *
     * @param tipo tipo del nodo
     */
    public NodoAST(String tipo) {
        this.tipo = tipo;
        this.hijos = new ArrayList<>();
    }

    /**
     * Constructor con tipo y valor.
     *
     * @param tipo tipo del nodo
     * @param valor valor asociado al nodo
     */
    public NodoAST(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    /**
     * Agrega un nodo hijo.
     *
     * @param hijo nodo que será agregado
     */
    public void agregarHijo(NodoAST hijo) {
        if (hijo != null) {
            hijos.add(hijo);
        }
    }

    /**
     * Obtiene el tipo del nodo.
     *
     * @return tipo del nodo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo del nodo.
     *
     * @param tipo tipo del nodo
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtiene el valor del nodo.
     *
     * @return valor del nodo
     */
    public String getValor() {
        return valor;
    }

    /**
     * Establece el valor del nodo.
     *
     * @param valor valor del nodo
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * Obtiene la lista de hijos.
     *
     * @return lista de nodos hijos
     */
    public List<NodoAST> getHijos() {
        return hijos;
    }

    /**
     * Establece la lista de hijos.
     *
     * @param hijos lista de nodos hijos
     */
    public void setHijos(List<NodoAST> hijos) {
        this.hijos = hijos != null ? hijos : new ArrayList<>();
    }

    /**
     * Representación textual del árbol.
     *
     * @return representación del nodo
     */
    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * Construye recursivamente una representación
     * jerárquica del árbol.
     *
     * @param nivel nivel de indentación
     * @return texto del árbol
     */
    private String toString(int nivel) {

        StringBuilder resultado = new StringBuilder();

        resultado.append("  ".repeat(nivel));

        resultado.append(tipo);

        if (valor != null && !valor.isEmpty()) {
            resultado.append(": ").append(valor);
        }

        resultado.append("\n");

        for (NodoAST hijo : hijos) {
            resultado.append(hijo.toString(nivel + 1));
        }

        return resultado.toString();
    }
}


package edu.fpune.compila26.sintactico;


import java.util.ArrayList;
import java.util.List;

import edu.fpune.compila26.lexico.Token;

/**
 * Analizador sintáctico del compilador Compila26.
 *
 * Implementa un parser descendente recursivo
 * basado en la gramática definida para el lenguaje.
 */
public class Parser {

    private List<Token> tokens;
    private int posicion;
    private Token tokenActual;
    private List<String> errores = new ArrayList<>();

    /**
     * Constructor del parser.
     *
     * @param tokens lista de tokens generados por el analizador léxico
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.posicion = 0;
        this.tokenActual = tokens.isEmpty() ? null : tokens.get(0);
    }

    // =============================
    // MÉTODO PRINCIPAL
    // =============================

    /**
     * Inicia el análisis sintáctico.
     *
     * @return raíz del Árbol de Sintaxis Abstracta (AST)
     */
    public NodoAST parse() {
        return programa();
    }

    // =============================
    // GRAMÁTICA
    // =============================

    /*
     * programa → sentencia*
     */
    private NodoAST programa() {

        NodoAST raiz = new NodoAST("PROGRAMA");

        while (tokenActual != null) {
            raiz.agregarHijo(sentencia());
        }

        return raiz;
    }

    /*
     * sentencia → declaracion | asignacion
     */
    private NodoAST sentencia() {
        if (tokenActual != null && tokenActual.getValor().equals("int")) {
            return declaracion();
        }

        return asignacion();
    }

    /*
     * declaracion → 'int' IDENTIFICADOR ';'
     */
    private NodoAST declaracion() {
        NodoAST nodo = new NodoAST("DECLARACION", "int");

        consumir("int");

        if (tokenActual == null) {
            error("Se esperaba identificador");
            return nodo;
        }

        String id = tokenActual.getValor();
        consumirTipo("IDENTIFICADOR");
        nodo.agregarHijo(new NodoAST("ID", id));
        consumir(";");

        return nodo;
    }

    /*
     * asignacion → IDENTIFICADOR '=' expresion ';'
     */
    private NodoAST asignacion() {

        NodoAST nodo = new NodoAST("ASIGNACION");

        if (tokenActual == null) {
            error("Se esperaba identificador");
            return nodo;
        }

        String id = tokenActual.getValor();

        consumirTipo("IDENTIFICADOR");

        NodoAST nodoId = new NodoAST("ID", id);

        consumir("=");

        NodoAST expr = expresion();

        consumir(";");

        nodo.agregarHijo(nodoId);
        nodo.agregarHijo(expr);

        return nodo;
    }

    /*
     * expresion → suma
     */
    private NodoAST expresion() {
        return suma();
    }

    /*
     * suma → termino ((+ | -) termino)*
     */
    private NodoAST suma() {

        NodoAST nodo = termino();

        while (tokenActual != null
                && (tokenActual.getValor().equals("+")
                || tokenActual.getValor().equals("-"))) {

            String op = tokenActual.getValor();

            avanzar();

            NodoAST derecho = termino();

            NodoAST nuevo = new NodoAST("OPERADOR", op);

            nuevo.agregarHijo(nodo);
            nuevo.agregarHijo(derecho);

            nodo = nuevo;
        }

        return nodo;
    }

    /*
     * termino → factor ((* | /) factor)*
     */
    private NodoAST termino() {

        NodoAST nodo = factor();

        while (tokenActual != null
                && (tokenActual.getValor().equals("*")
                || tokenActual.getValor().equals("/"))) {

            String op = tokenActual.getValor();

            avanzar();

            NodoAST derecho = factor();

            NodoAST nuevo = new NodoAST("OPERADOR", op);

            nuevo.agregarHijo(nodo);
            nuevo.agregarHijo(derecho);

            nodo = nuevo;
        }

        return nodo;
    }

    /*
     * factor → NUMERO | IDENTIFICADOR | '(' expresion ')'
     */
    private NodoAST factor() {

        if (tokenActual == null) {
            error("Expresión incompleta");
            return new NodoAST("ERROR");
        }

        // =============================
        // Número
        // =============================

        if (tokenActual.getTipo().equals("NUMERO")) {

            NodoAST nodo =
                    new NodoAST("NUMERO", tokenActual.getValor());

            avanzar();

            return nodo;
        }

        // =============================
        // Identificador
        // =============================

        if (tokenActual.getTipo().equals("IDENTIFICADOR")) {

            NodoAST nodo =
                    new NodoAST("ID", tokenActual.getValor());

            avanzar();

            return nodo;
        }

        // =============================
        // Paréntesis
        // =============================

        if (tokenActual.getValor().equals("(")) {

            avanzar();

            NodoAST nodo = expresion();

            consumir(")");

            return nodo;
        }

        // =============================
        // Error
        // =============================

        error("Factor inválido: " + tokenActual.getValor());

        avanzar();

        return new NodoAST("ERROR");
    }

    // =============================
    // UTILIDADES
    // =============================

    /**
     * Avanza al siguiente token.
     */
    private void avanzar() {

        posicion++;

        if (posicion < tokens.size()) {
            tokenActual = tokens.get(posicion);
        } else {
            tokenActual = null;
        }
    }

    /**
     * Consume un token cuyo valor debe coincidir
     * con el valor esperado.
     *
     * @param esperado valor esperado
     */
    private void consumir(String esperado) {

        if (tokenActual != null
                && tokenActual.getValor().equals(esperado)) {

            avanzar();

        } else {

            error("Se esperaba '" + esperado + "'");
        }
    }

    /**
     * Consume un token cuyo tipo debe coincidir
     * con el tipo esperado.
     *
     * @param tipo tipo esperado
     */
    private void consumirTipo(String tipo) {

        if (tokenActual != null
                && tokenActual.getTipo().equals(tipo)) {

            avanzar();

        } else {

            error("Se esperaba tipo: " + tipo);
        }
    }

    // =============================
    // MANEJO DE ERRORES
    // =============================

    /**
     * Registra un error sintáctico y aplica
     * recuperación mediante modo pánico.
     *
     * @param mensaje descripción del error
     */
    private void error(String mensaje) {

        String errorMsg =
                "Error sintáctico: "
                        + mensaje
                        + (tokenActual != null
                        ? " cerca de '" + tokenActual.getValor() + "'"
                        : "");

        errores.add(errorMsg);

        // Recuperación en modo pánico.
        while (tokenActual != null
                && !tokenActual.getValor().equals(";")
                && !tokenActual.getValor().equals("}")) {

            avanzar();
        }

        if (tokenActual != null) {
            avanzar();
        }
    }

    /**
     * Devuelve los errores sintácticos encontrados.
     *
     * @return lista de errores
     */
    public List<String> getErrores() {
        return errores;
    }

    /**
     * Indica si el análisis sintáctico fue exitoso.
     *
     * @return true si no se encontraron errores
     */
    public boolean esExitoso() {
        return errores.isEmpty();
    }
}

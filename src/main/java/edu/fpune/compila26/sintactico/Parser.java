package edu.fpune.compila26.sintactico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import edu.fpune.compila26.lexico.Token;

/** Analizador sintactico descendente recursivo de Compila26. */
public class Parser {

    public static final int MAX_EVENTOS_TRAZA_PREDETERMINADO = 1000;

    private final List<Token> tokens;
    private int posicion;
    private Token tokenActual;
    private final List<String> errores = new ArrayList<>();
    private final List<PasoSintactico> traza;
    private final int maxEventosTraza;
    private final Deque<String> funcionesActivas = new ArrayDeque<>();
    private boolean trazaTruncada;
    private boolean recuperando;
    private int profundidad;

    public Parser(List<Token> tokens) {
        this(tokens, false, MAX_EVENTOS_TRAZA_PREDETERMINADO);
    }

    public Parser(List<Token> tokens, boolean incluirTraza) {
        this(tokens, incluirTraza, MAX_EVENTOS_TRAZA_PREDETERMINADO);
    }

    public Parser(List<Token> tokens, boolean incluirTraza, int maxEventosTraza) {
        if (maxEventosTraza < 1) {
            throw new IllegalArgumentException("El limite de eventos debe ser mayor que cero");
        }
        this.tokens = tokens;
        this.posicion = 0;
        this.tokenActual = tokens.isEmpty() ? null : tokens.get(0);
        this.maxEventosTraza = maxEventosTraza;
        this.traza = incluirTraza ? new ArrayList<>() : null;
    }

    public NodoAST parse() {
        return programa();
    }

    private NodoAST programa() {
        entrar("programa", "programa -> { sentencia }",
                "El parser comienza a reconocer una secuencia de sentencias.");
        produccion("programa -> { sentencia }",
                "Se procesan sentencias mientras exista un token de anticipacion.");
        NodoAST raiz = crearNodo("PROGRAMA", null, tokenActual,
                "Se crea la raiz del AST del programa.");
        while (tokenActual != null) {
            raiz.agregarHijo(sentencia());
        }
        salir("programa", "Finalizo el reconocimiento del programa.");
        return raiz;
    }

    private NodoAST sentencia() {
        entrar("sentencia", "sentencia -> declaracion | asignacion",
                "El token de anticipacion permite elegir la clase de sentencia.");
        anticipacion("Se inspecciona el token sin consumirlo para elegir una alternativa.");
        NodoAST nodo;
        if (tokenActual != null && tokenActual.getValor().equals("int")) {
            produccion("sentencia -> declaracion", "La palabra reservada 'int' inicia una declaracion.");
            nodo = declaracion();
        } else {
            produccion("sentencia -> asignacion",
                    "Un token diferente de 'int' se analiza como inicio de una asignacion.");
            nodo = asignacion();
        }
        salir("sentencia", "Finalizo el reconocimiento de la sentencia.");
        return nodo;
    }

    private NodoAST declaracion() {
        entrar("declaracion", "declaracion -> 'int' IDENTIFICADOR ';'",
                "Se reconoce una declaracion de variable entera.");
        produccion("declaracion -> 'int' IDENTIFICADOR ';'",
                "La declaracion contiene palabra reservada, identificador y punto y coma.");
        NodoAST nodo = crearNodo("DECLARACION", "int", tokenActual,
                "Se crea el nodo que representa la declaracion.");
        consumir("int");
        if (tokenActual == null) {
            error("Se esperaba identificador");
            salir("declaracion", "La declaracion finaliza con un error y un AST parcial.");
            return nodo;
        }
        Token tokenId = tokenActual;
        String id = tokenActual.getValor();
        consumirTipo("IDENTIFICADOR");
        nodo.agregarHijo(crearNodo("ID", id, tokenId, "Se crea el identificador declarado en el AST."));
        consumir(";");
        salir("declaracion", "Finalizo el reconocimiento de la declaracion.");
        return nodo;
    }

    private NodoAST asignacion() {
        entrar("asignacion", "asignacion -> IDENTIFICADOR '=' expresion ';'",
                "Se reconoce una asignacion y luego su expresion.");
        produccion("asignacion -> IDENTIFICADOR '=' expresion ';'",
                "La asignacion comienza con un identificador.");
        NodoAST nodo = crearNodo("ASIGNACION", null, tokenActual,
                "Se crea el nodo que representa la asignacion.");
        if (tokenActual == null) {
            error("Se esperaba identificador");
            salir("asignacion", "La asignacion finaliza con un error y un AST parcial.");
            return nodo;
        }
        Token tokenId = tokenActual;
        String id = tokenActual.getValor();
        consumirTipo("IDENTIFICADOR");
        NodoAST nodoId = crearNodo("ID", id, tokenId, "Se crea el identificador que recibira el valor.");
        consumir("=");
        NodoAST expr = expresion();
        consumir(";");
        nodo.agregarHijo(nodoId);
        nodo.agregarHijo(expr);
        salir("asignacion", "Finalizo el reconocimiento de la asignacion.");
        return nodo;
    }

    private NodoAST expresion() {
        entrar("expresion", "expresion -> suma", "La expresion delega en la produccion de suma.");
        produccion("expresion -> suma", "Se inicia el nivel de suma y resta.");
        NodoAST nodo = suma();
        salir("expresion", "Finalizo el reconocimiento de la expresion.");
        return nodo;
    }

    private NodoAST suma() {
        entrar("suma", "suma -> termino { ('+' | '-') termino }",
                "La suma analiza terminos completos antes de combinar suma o resta.");
        produccion("suma -> termino { ('+' | '-') termino }",
                "Primero se reconoce un termino para respetar la precedencia.");
        NodoAST nodo = termino();
        while (tokenActual != null
                && (tokenActual.getValor().equals("+") || tokenActual.getValor().equals("-"))) {
            Token tokenOperador = tokenActual;
            String op = tokenActual.getValor();
            produccion("suma -> suma ('+' | '-') termino",
                    "El operador aditivo extiende la expresion de izquierda a derecha.");
            avanzar();
            NodoAST derecho = termino();
            NodoAST nuevo = crearNodo("OPERADOR", op, tokenOperador,
                    "Se crea el nodo aditivo despues de construir su termino derecho.");
            nuevo.agregarHijo(nodo);
            nuevo.agregarHijo(derecho);
            nodo = nuevo;
        }
        salir("suma", "Finalizo el nivel de suma y resta.");
        return nodo;
    }

    private NodoAST termino() {
        entrar("termino", "termino -> factor { ('*' | '/') factor }",
                "Los terminos agrupan multiplicacion y division antes que suma y resta.");
        produccion("termino -> factor { ('*' | '/') factor }", "Primero se reconoce un factor.");
        NodoAST nodo = factor();
        while (tokenActual != null
                && (tokenActual.getValor().equals("*") || tokenActual.getValor().equals("/"))) {
            Token tokenOperador = tokenActual;
            String op = tokenActual.getValor();
            produccion("termino -> termino ('*' | '/') factor",
                    "El operador multiplicativo se resuelve dentro del termino.");
            avanzar();
            NodoAST derecho = factor();
            NodoAST nuevo = crearNodo("OPERADOR", op, tokenOperador,
                    "Se crea el nodo multiplicativo antes de retornar al nivel aditivo.");
            nuevo.agregarHijo(nodo);
            nuevo.agregarHijo(derecho);
            nodo = nuevo;
        }
        salir("termino", "Finalizo el nivel de multiplicacion y division.");
        return nodo;
    }

    private NodoAST factor() {
        entrar("factor", "factor -> NUMERO | IDENTIFICADOR | '(' expresion ')'",
                "El factor selecciona una alternativa mediante el token de anticipacion.");
        anticipacion("Se inspecciona el token para seleccionar una alternativa de factor.");
        if (tokenActual == null) {
            produccion("factor -> error", "No existe token de anticipacion para completar el factor.");
            error("Expresi\u00f3n incompleta");
            NodoAST nodo = crearNodo("ERROR", null, null, "Se conserva un nodo de error en el AST parcial.");
            salir("factor", "El factor finaliza con un error al final de la entrada.");
            return nodo;
        }
        if (tokenActual.getTipo().equals("NUMERO")) {
            produccion("factor -> NUMERO", "El token NUMERO coincide con una alternativa de factor.");
            NodoAST nodo = crearNodo("NUMERO", tokenActual.getValor(), tokenActual,
                    "Se crea un nodo numerico en el AST.");
            avanzar();
            salir("factor", "Finalizo el reconocimiento del factor numerico.");
            return nodo;
        }
        if (tokenActual.getTipo().equals("IDENTIFICADOR")) {
            produccion("factor -> IDENTIFICADOR",
                    "El token IDENTIFICADOR coincide con una alternativa de factor.");
            NodoAST nodo = crearNodo("ID", tokenActual.getValor(), tokenActual,
                    "Se crea un nodo identificador en el AST.");
            avanzar();
            salir("factor", "Finalizo el reconocimiento del factor identificador.");
            return nodo;
        }
        if (tokenActual.getValor().equals("(")) {
            produccion("factor -> '(' expresion ')'", "El parentesis abre una expresion anidada.");
            avanzar();
            NodoAST nodo = expresion();
            consumir(")");
            salir("factor", "Finalizo el reconocimiento del factor entre parentesis.");
            return nodo;
        }
        Token tokenError = tokenActual;
        error("Factor inv\u00e1lido: " + tokenActual.getValor());
        avanzar();
        NodoAST nodo = crearNodo("ERROR", null, tokenError, "Se conserva un nodo de error en el AST parcial.");
        salir("factor", "El factor finaliza con un token invalido.");
        return nodo;
    }

    private void avanzar() {
        if (tokenActual != null) {
            registrar(recuperando ? PasoSintactico.Evento.DESCARTAR_TOKEN : PasoSintactico.Evento.CONSUMIR_TOKEN,
                    funcionActual(), null, tokenActual,
                    recuperando ? "Se descarta el token durante la recuperacion en modo panico."
                            : "El token coincide con lo esperado y el parser avanza.", null, null);
        }
        posicion++;
        tokenActual = posicion < tokens.size() ? tokens.get(posicion) : null;
    }

    private void consumir(String esperado) {
        if (tokenActual != null && tokenActual.getValor().equals(esperado)) {
            avanzar();
        } else {
            error("Se esperaba '" + esperado + "'");
        }
    }

    private void consumirTipo(String tipo) {
        if (tokenActual != null && tokenActual.getTipo().equals(tipo)) {
            avanzar();
        } else {
            error("Se esperaba tipo: " + tipo);
        }
    }

    private void error(String mensaje) {
        String errorMsg = "Error sint\u00e1ctico: " + mensaje
                + (tokenActual != null ? " cerca de '" + tokenActual.getValor() + "' en l\u00ednea "
                        + tokenActual.getLinea() + ", columna " + tokenActual.getColumna() : ubicacionFinDeEntrada());
        errores.add(errorMsg);
        registrar(PasoSintactico.Evento.ERROR_SINTACTICO, funcionActual(), null,
                tokenActual, errorMsg, null, null);
        registrar(PasoSintactico.Evento.INICIAR_RECUPERACION, funcionActual(), null, tokenActual,
                "Se inicia recuperacion en modo panico hasta ';', '}' o el final de la entrada.", null, null);
        recuperando = true;
        while (tokenActual != null && !tokenActual.getValor().equals(";") && !tokenActual.getValor().equals("}")) {
            avanzar();
        }
        if (tokenActual != null) {
            avanzar();
        }
        recuperando = false;
        registrar(PasoSintactico.Evento.FINALIZAR_RECUPERACION, funcionActual(), null,
                tokenActual, "Finalizo la recuperacion; el analisis puede continuar.", null, null);
    }

    private String ubicacionFinDeEntrada() {
        if (tokens.isEmpty()) {
            return " al final de la entrada (EOF), l\u00ednea 1, columna 1";
        }
        Token ultimo = tokens.get(tokens.size() - 1);
        return " al final de la entrada (EOF), l\u00ednea " + ultimo.getLinea()
                + ", columna " + (ultimo.getColumna() + ultimo.getValor().length());
    }

    public List<String> getErrores() {
        return errores;
    }

    public boolean esExitoso() {
        return errores.isEmpty();
    }

    public List<PasoSintactico> getTraza() {
        return traza == null ? List.of() : List.copyOf(traza);
    }

    public boolean isTrazaTruncada() {
        return trazaTruncada;
    }

    private void entrar(String funcion, String produccion, String descripcion) {
        registrar(PasoSintactico.Evento.ENTRAR_FUNCION, funcion, produccion, tokenActual, descripcion, null, null);
        funcionesActivas.push(funcion);
        profundidad++;
    }

    private void salir(String funcion, String descripcion) {
        profundidad--;
        registrar(PasoSintactico.Evento.SALIR_FUNCION, funcion, null, tokenActual, descripcion, null, null);
        funcionesActivas.pop();
    }

    private void anticipacion(String descripcion) {
        registrar(PasoSintactico.Evento.ANTICIPACION, funcionActual(), null, tokenActual, descripcion, null, null);
    }

    private void produccion(String produccion, String descripcion) {
        registrar(PasoSintactico.Evento.SELECCIONAR_PRODUCCION, funcionActual(), produccion,
                tokenActual, descripcion, null, null);
    }

    private NodoAST crearNodo(String tipo, String valor, Token tokenOrigen, String descripcion) {
        NodoAST nodo = valor == null ? new NodoAST(tipo) : new NodoAST(tipo, valor);
        registrar(PasoSintactico.Evento.CONSTRUIR_NODO, funcionActual(), null,
                tokenOrigen, descripcion, tipo, valor);
        return nodo;
    }

    private String funcionActual() {
        return funcionesActivas.peek();
    }

    private void registrar(PasoSintactico.Evento evento, String funcion, String produccion,
            Token token, String descripcion, String nodoTipo, String nodoValor) {
        if (traza == null) {
            return;
        }
        if (traza.size() >= maxEventosTraza) {
            trazaTruncada = true;
            return;
        }
        traza.add(new PasoSintactico(traza.size() + 1, evento, funcion, produccion,
                token == null ? null : token.getValor(), token == null ? null : token.getTipo(),
                token == null ? null : token.getLinea(), token == null ? null : token.getColumna(),
                profundidad, descripcion, nodoTipo, nodoValor));
    }
}

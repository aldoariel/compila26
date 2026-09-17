package edu.fpune.compila26.semantico;

import java.util.List;

import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

public class PruebaSemantico {

    public static void main(String[] args) {

        //String codigo = "x = 10; x = x + 5;x = y + 10;";
        String codigo = "x = 10 + 5 * 2;";
        System.out.println("=================================");
        System.out.println("       PRUEBA ANALISIS SEMANTICO");
        System.out.println("=================================");

        System.out.println();
        System.out.println("Código:");
        System.out.println(codigo);

        // 1. LÉXICO
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.analizar(codigo);

        // 2. SINTÁCTICO
        Parser parser = new Parser(tokens);
        NodoAST ast = parser.parse();

        System.out.println();
        System.out.println("AST:");
        System.out.println(ast);

        if (!parser.esExitoso()) {

            System.out.println();
            System.out.println("Errores sintácticos:");

            for (String error : parser.getErrores()) {
                System.out.println("  " + error);
            }

            return;
        }

        // 3. SEMÁNTICO
        AnalizadorSemantico semantico =
                new AnalizadorSemantico();

        boolean correcto =
                semantico.analizar(ast);

        System.out.println();
        System.out.println("Resultado semántico:");

        if (correcto) {
            System.out.println("  ✓ Análisis semántico correcto.");
        } else {

            System.out.println("  ✗ Se encontraron errores:");

            for (String error : semantico.getErrores()) {
                System.out.println("    " + error);
            }
        }

        // 4. TABLA DE SÍMBOLOS
        System.out.println();
        System.out.println("Tabla de símbolos:");

        for (var simbolo :
                semantico.getTabla().getSimbolos()) {

            System.out.println("  " + simbolo);
        }
    }
}
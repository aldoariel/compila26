package edu.fpune.compila26.sintactico;



import java.util.List;

import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;


public class PruebaParser {

    public static void main(String[] args) {

        //String codigo = "x = 10 + 5 * 2;";
       // String codigo =  "x = (10 + 5) * 2;";
         String codigo =  "x = 10 +" ;

        System.out.println("=================================");
        System.out.println("     PRUEBA LEXER + PARSER");
        System.out.println("=================================");
        System.out.println();

        System.out.println("Código fuente:");
        System.out.println(codigo);
        System.out.println();

        // ==========================================
        // 1. ANÁLISIS LÉXICO
        // ==========================================

        Lexer lexer = new Lexer();

        List<Token> tokens = lexer.analizar(codigo);

        System.out.println("TOKENS:");
        System.out.println("---------------------------------");

        for (Token token : tokens) {
            System.out.println(
                    token.getTipo()
                            + " -> "
                            + token.getValor()
            );
        }

        System.out.println();

        // ==========================================
        // 2. ANÁLISIS SINTÁCTICO
        // ==========================================

        Parser parser = new Parser(tokens);

        NodoAST ast = parser.parse();

        System.out.println("ÁRBOL AST:");
        System.out.println("---------------------------------");
        System.out.println(ast);

        // ==========================================
        // 3. RESULTADO
        // ==========================================

        System.out.println("RESULTADO:");
        System.out.println("---------------------------------");

        if (parser.esExitoso()) {
            System.out.println("✓ Análisis sintáctico exitoso.");
        } else {
            System.out.println("✗ Se encontraron errores:");

            for (String error : parser.getErrores()) {
                System.out.println("  - " + error);
            }
        }
    }
}
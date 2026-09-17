package edu.fpune.compila26.service;


import java.util.List;

import org.springframework.stereotype.Service;

import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class ParserService {

    private final Lexer lexer;

    public ParserService() {
        this.lexer = new Lexer();
    }

    public ResultadoParser analizar(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return new ResultadoParser(
                    false,
                    "El código fuente está vacío.",
                    null,
                    List.of()
            );
        }

        // 1. Análisis léxico
        List<Token> tokens = lexer.analizar(codigo);

        // 2. Análisis sintáctico
        Parser parser = new Parser(tokens);
        NodoAST ast = parser.parse();

        // 3. Resultado
        boolean exitoso = parser.esExitoso();

        String mensaje = exitoso
                ? "Análisis sintáctico exitoso."
                : "Se encontraron errores sintácticos.";

        return new ResultadoParser(
                exitoso,
                mensaje,
                ast,
                parser.getErrores()
        );
    }

    public record ResultadoParser(
            boolean exitoso,
            String mensaje,
            NodoAST ast,
            List<String> errores
    ) {
    }
}
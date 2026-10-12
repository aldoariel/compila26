package edu.fpune.compila26.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class ParserService {

    private final ValidadorCodigoFuente validadorCodigoFuente;

    public ParserService() {
        this(new ValidadorCodigoFuente());
    }

    @Autowired
    public ParserService(ValidadorCodigoFuente validadorCodigoFuente) {
        this.validadorCodigoFuente = validadorCodigoFuente;
    }

    public ResultadoParser analizar(String codigo) {

        validadorCodigoFuente.validarTamanio(codigo);

        if (codigo == null || codigo.isBlank()) {
            return new ResultadoParser(
                    false,
                    "El código fuente está vacío.",
                    null,
                    List.of()
            );
        }

        // 1. Análisis léxico
        AnalisisLexicoFuente.Resultado resultadoLexico = AnalisisLexicoFuente.analizar(codigo);
        List<Token> tokens = resultadoLexico.tokens();

        if (resultadoLexico.tieneErrores()) {
            return new ResultadoParser(
                    false,
                    "Se encontraron errores l\u00e9xicos.",
                    null,
                    resultadoLexico.errores()
            );
        }

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

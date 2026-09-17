package edu.fpune.compila26.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.fpune.compila26.intermedio.GeneradorIntermedio;
import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class IntermedioService {

    public ResultadoIntermedio generar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return error("El código fuente está vacío.", List.of("El código fuente está vacío."));
        }

        List<Token> tokens = new Lexer().analizar(codigo);
        List<String> erroresLexicos = tokens.stream()
                .filter(token -> "ERROR".equals(token.getTipo()))
                .map(this::formatearErrorLexico)
                .toList();

        if (!erroresLexicos.isEmpty()) {
            return error("Se encontraron errores léxicos.", erroresLexicos);
        }

        Parser parser = new Parser(tokens);
        NodoAST ast = parser.parse();

        if (!parser.esExitoso()) {
            return error("Se encontraron errores sintácticos.", parser.getErrores());
        }

        AnalizadorSemantico semantico = new AnalizadorSemantico();

        if (!semantico.analizar(ast)) {
            return error("Se encontraron errores semánticos.", semantico.getErrores());
        }

        List<String> codigoIntermedio = new GeneradorIntermedio()
                .generarInstrucciones(ast)
                .stream()
                .map(InstruccionIntermedia::formatear)
                .toList();

        return new ResultadoIntermedio(
                true,
                "Código intermedio generado correctamente.",
                codigoIntermedio,
                List.of()
        );
    }

    private ResultadoIntermedio error(String mensaje, List<String> errores) {
        return new ResultadoIntermedio(false, mensaje, List.of(), errores);
    }

    private String formatearErrorLexico(Token token) {
        return "Error léxico: carácter no reconocido '"
                + token.getValor()
                + "' en línea "
                + token.getLinea()
                + ", columna "
                + token.getColumna()
                + ".";
    }

    public record ResultadoIntermedio(
            boolean exitoso,
            String mensaje,
            List<String> codigoIntermedio,
            List<String> errores
    ) {
    }
}

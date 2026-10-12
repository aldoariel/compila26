package edu.fpune.compila26.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.intermedio.GeneradorIntermedio;
import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class IntermedioService {

    private final ValidadorCodigoFuente validadorCodigoFuente;

    public IntermedioService() {
        this(new ValidadorCodigoFuente());
    }

    @Autowired
    public IntermedioService(ValidadorCodigoFuente validadorCodigoFuente) {
        this.validadorCodigoFuente = validadorCodigoFuente;
    }

    public ResultadoIntermedio generar(String codigo) {
        validadorCodigoFuente.validarTamanio(codigo);
        if (codigo == null || codigo.isBlank()) {
            return error("El código fuente está vacío.", List.of("El código fuente está vacío."));
        }

        AnalisisLexicoFuente.Resultado resultadoLexico = AnalisisLexicoFuente.analizar(codigo);
        List<Token> tokens = resultadoLexico.tokens();
        List<String> erroresLexicos = resultadoLexico.errores();

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

    public record ResultadoIntermedio(
            boolean exitoso,
            String mensaje,
            List<String> codigoIntermedio,
            List<String> errores
    ) {
    }
}

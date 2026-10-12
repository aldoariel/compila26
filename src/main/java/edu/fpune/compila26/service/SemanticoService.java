package edu.fpune.compila26.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class SemanticoService {

    private final ValidadorCodigoFuente validadorCodigoFuente;

    public SemanticoService() {
        this(new ValidadorCodigoFuente());
    }

    @Autowired
    public SemanticoService(ValidadorCodigoFuente validadorCodigoFuente) {
        this.validadorCodigoFuente = validadorCodigoFuente;
    }

    /**
     * Ejecuta el proceso completo de análisis:
     *
     * Código fuente
     *      ↓
     * Lexer
     *      ↓
     * Parser
     *      ↓
     * AST
     *      ↓
     * Analizador semántico
     */
    public ResultadoSemantico analizar(String codigo) {

        validadorCodigoFuente.validarTamanio(codigo);

        // =========================================
        // VALIDACIÓN
        // =========================================

        if (codigo == null || codigo.isBlank()) {

            return new ResultadoSemantico(
                    false,
                    "El código fuente está vacío.",
                    List.of(),
                    List.of()
            );
        }

        // =========================================
        // 1. ANÁLISIS LÉXICO
        // =========================================

        AnalisisLexicoFuente.Resultado resultadoLexico =
                AnalisisLexicoFuente.analizar(codigo);
        List<Token> tokens = resultadoLexico.tokens();

        if (resultadoLexico.tieneErrores()) {
            return new ResultadoSemantico(
                    false,
                    "Se encontraron errores l\u00e9xicos.",
                    resultadoLexico.errores(),
                    List.of()
            );
        }

        // =========================================
        // 2. ANÁLISIS SINTÁCTICO
        // =========================================

        Parser parser =
                new Parser(tokens);

        NodoAST ast =
                parser.parse();

        // =========================================
        // SI HAY ERRORES SINTÁCTICOS
        // =========================================

        if (!parser.esExitoso()) {

            return new ResultadoSemantico(
                    false,
                    "Se encontraron errores sintácticos.",
                    parser.getErrores(),
                    List.of()
            );
        }

        // =========================================
        // 3. ANÁLISIS SEMÁNTICO
        // =========================================

        AnalizadorSemantico semantico =
                new AnalizadorSemantico();

        boolean exitoso =
                semantico.analizar(ast);

        String mensaje;

        if (exitoso) {

            mensaje =
                    "Análisis semántico exitoso.";

        } else {

            mensaje =
                    "Se encontraron errores semánticos.";
        }

        // =========================================
        // 4. CONSTRUIR LISTA DE SÍMBOLOS
        // =========================================

        List<SimboloDto> simbolos =
                semantico.getTabla()
                        .getSimbolos()
                        .stream()
                        .map(simbolo ->
                                new SimboloDto(
                                        simbolo.getNombre(),
                                        simbolo.getTipo()
                                )
                        )
                        .toList();

        // =========================================
        // 5. DEVOLVER RESULTADO
        // =========================================

        return new ResultadoSemantico(
                exitoso,
                mensaje,
                semantico.getErrores(),
                simbolos
        );
    }

    // =============================================
    // DTO DEL SÍMBOLO
    // =============================================

    public record SimboloDto(
            String nombre,
            String tipo
    ) {
    }

    // =============================================
    // RESULTADO DEL ANÁLISIS
    // =============================================

    public record ResultadoSemantico(
            boolean exitoso,
            String mensaje,
            List<String> errores,
            List<SimboloDto> simbolos
    ) {
    }
}

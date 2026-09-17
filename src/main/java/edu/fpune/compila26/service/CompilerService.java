package edu.fpune.compila26.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.fpune.compila26.backend.neander.DatoNeander;
import edu.fpune.compila26.backend.neander.GeneradorNeander;
import edu.fpune.compila26.backend.neander.ResultadoGeneracionNeander;
import edu.fpune.compila26.intermedio.GeneradorIntermedio;
import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.lexico.TokenDto;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;

@Service
public class CompilerService {

    public ResultadoCompilacion compilar(String codigoFuente) {
        if (codigoFuente == null || codigoFuente.isBlank()) {
            return resultadoDetenido(
                    codigoFuente,
                    "El código fuente está vacío.",
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        List<Token> tokens = new Lexer().analizar(codigoFuente);
        List<TokenDto> tokenDtos = tokens.stream()
                .map(this::convertirToken)
                .toList();
        List<String> erroresLexicos = tokens.stream()
                .filter(token -> "ERROR".equals(token.getTipo()))
                .map(this::formatearErrorLexico)
                .toList();

        if (!erroresLexicos.isEmpty()) {
            return resultadoDetenido(
                    codigoFuente,
                    "Se encontraron errores léxicos.",
                    tokenDtos,
                    null,
                    erroresLexicos,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        Parser parser = new Parser(tokens);
        NodoAST ast = parser.parse();

        if (!parser.esExitoso()) {
            return resultadoDetenido(
                    codigoFuente,
                    "Se encontraron errores sintácticos.",
                    tokenDtos,
                    ast,
                    List.of(),
                    parser.getErrores(),
                    List.of(),
                    List.of()
            );
        }

        AnalizadorSemantico semantico = new AnalizadorSemantico();
        boolean semanticaValida = semantico.analizar(ast);
        List<SimboloCompilacion> simbolos = semantico.getTabla()
                .getSimbolos()
                .stream()
                .map(simbolo -> new SimboloCompilacion(simbolo.getNombre(), simbolo.getTipo()))
                .toList();

        if (!semanticaValida) {
            return resultadoDetenido(
                    codigoFuente,
                    "Se encontraron errores semánticos.",
                    tokenDtos,
                    ast,
                    List.of(),
                    List.of(),
                    semantico.getErrores(),
                    simbolos
            );
        }

        List<InstruccionIntermedia> instruccionesIntermedias =
                new GeneradorIntermedio().generarInstrucciones(ast);
        List<String> codigoIntermedio = instruccionesIntermedias
                .stream()
                .map(InstruccionIntermedia::formatear)
                .toList();
        ResultadoGeneracionNeander resultadoNeander =
                new GeneradorNeander().generar(instruccionesIntermedias);

        return new ResultadoCompilacion(
                true,
                "Compilación completada correctamente.",
                codigoFuente,
                tokenDtos,
                ast,
                List.of(),
                List.of(),
                List.of(),
                simbolos,
                codigoIntermedio,
                convertirResultadoNeander(resultadoNeander)
        );
    }

    private ResultadoCompilacion resultadoDetenido(
            String codigoFuente,
            String mensaje,
            List<TokenDto> tokens,
            NodoAST ast,
            List<String> erroresLexicos,
            List<String> erroresSintacticos,
            List<String> erroresSemanticos,
            List<SimboloCompilacion> simbolos) {

        return new ResultadoCompilacion(
                false,
                mensaje,
                codigoFuente,
                tokens,
                ast,
                erroresLexicos,
                erroresSintacticos,
                erroresSemanticos,
                simbolos,
                List.of(),
                ResultadoNeanderCompilacion.noEjecutado()
        );
    }

    private ResultadoNeanderCompilacion convertirResultadoNeander(
            ResultadoGeneracionNeander resultado) {
        return new ResultadoNeanderCompilacion(
                true,
                resultado.exitoso(),
                resultado.ensamblador(),
                resultado.datos().stream().map(this::convertirDatoNeander).toList(),
                resultado.diagnosticos()
        );
    }

    private DatoNeanderCompilacion convertirDatoNeander(DatoNeander dato) {
        if (dato.temporalInterno()) {
            return new DatoNeanderCompilacion(
                    "TEMPORAL_INTERNO",
                    "Temporal interno del backend",
                    dato.direccionHexadecimal(),
                    dato.valorInicial()
            );
        }

        return new DatoNeanderCompilacion(
                dato.operando().tipo().name(),
                dato.operando().valor(),
                dato.direccionHexadecimal(),
                dato.valorInicial()
        );
    }

    private TokenDto convertirToken(Token token) {
        return new TokenDto(
                token.getTipo(),
                token.getValor(),
                token.getLinea(),
                token.getColumna()
        );
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

    public record SimboloCompilacion(String nombre, String tipo) {
    }

    public record DatoNeanderCompilacion(
            String tipo,
            String nombre,
            String direccion,
            Integer valorInicial) {
    }

    public record ResultadoNeanderCompilacion(
            boolean ejecutado,
            boolean exitoso,
            List<String> instrucciones,
            List<DatoNeanderCompilacion> datos,
            List<String> diagnosticos) {

        public static ResultadoNeanderCompilacion noEjecutado() {
            return new ResultadoNeanderCompilacion(
                    false, false, List.of(), List.of(), List.of());
        }
    }

    public record ResultadoCompilacion(
            boolean exitoso,
            String mensaje,
            String codigoFuente,
            List<TokenDto> tokens,
            NodoAST ast,
            List<String> erroresLexicos,
            List<String> erroresSintacticos,
            List<String> erroresSemanticos,
            List<SimboloCompilacion> simbolos,
            List<String> codigoIntermedio,
            ResultadoNeanderCompilacion neander
    ) {
    }
}

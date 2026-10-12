package edu.fpune.compila26.service;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.backend.neander.DatoNeander;
import edu.fpune.compila26.backend.neander.GeneradorNeander;
import edu.fpune.compila26.backend.neander.ResultadoGeneracionNeander;
import edu.fpune.compila26.intermedio.GeneradorIntermedio;
import edu.fpune.compila26.intermedio.InstruccionIntermedia;
import edu.fpune.compila26.lexico.Token;
import edu.fpune.compila26.lexico.TokenDto;
import edu.fpune.compila26.semantico.AnalizadorSemantico;
import edu.fpune.compila26.sintactico.NodoAST;
import edu.fpune.compila26.sintactico.Parser;
import edu.fpune.compila26.sintactico.PasoSintactico;

@Service
public class CompilerService {

    private final ValidadorCodigoFuente validadorCodigoFuente;

    public CompilerService() {
        this(new ValidadorCodigoFuente());
    }

    @Autowired
    public CompilerService(ValidadorCodigoFuente validadorCodigoFuente) {
        this.validadorCodigoFuente = validadorCodigoFuente;
    }

    public ResultadoCompilacion compilar(String codigoFuente) {
        return compilar(codigoFuente, false);
    }

    public ResultadoCompilacion compilar(String codigoFuente, boolean incluirTraza) {
        validadorCodigoFuente.validarTamanio(codigoFuente);
        if (codigoFuente == null || codigoFuente.isBlank()) {
            return resultadoDetenido(
                    codigoFuente,
                    "El código fuente está vacío.",
                    List.of(),
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    trazaNoEjecutada(incluirTraza),
                    indicadorTraza(incluirTraza, false)
            );
        }

        AnalisisLexicoFuente.Resultado resultadoLexico = AnalisisLexicoFuente.analizar(codigoFuente);
        List<Token> tokens = resultadoLexico.tokens();
        List<TokenDto> tokenDtos = tokens.stream()
                .map(this::convertirToken)
                .toList();
        List<String> erroresLexicos = resultadoLexico.errores();

        if (!erroresLexicos.isEmpty()) {
            return resultadoDetenido(
                    codigoFuente,
                    "Se encontraron errores léxicos.",
                    tokenDtos,
                    null,
                    erroresLexicos,
                    List.of(),
                    List.of(),
                    List.of(),
                    trazaNoEjecutada(incluirTraza),
                    indicadorTraza(incluirTraza, false)
            );
        }

        Parser parser = new Parser(tokens, incluirTraza);
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
                    List.of(),
                    traza(parser, incluirTraza),
                    indicadorTraza(incluirTraza, parser.isTrazaTruncada())
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
                    simbolos,
                    traza(parser, incluirTraza),
                    indicadorTraza(incluirTraza, parser.isTrazaTruncada())
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
                convertirResultadoNeander(resultadoNeander),
                traza(parser, incluirTraza),
                indicadorTraza(incluirTraza, parser.isTrazaTruncada())
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
            List<SimboloCompilacion> simbolos,
            List<PasoSintactico> trazaSintactica,
            Boolean trazaSintacticaTruncada) {

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
                ResultadoNeanderCompilacion.noEjecutado(),
                trazaSintactica,
                trazaSintacticaTruncada
        );
    }

    private List<PasoSintactico> traza(Parser parser, boolean incluirTraza) {
        return incluirTraza ? parser.getTraza() : null;
    }

    private List<PasoSintactico> trazaNoEjecutada(boolean incluirTraza) {
        return incluirTraza ? List.of() : null;
    }

    private Boolean indicadorTraza(boolean incluirTraza, boolean truncada) {
        return incluirTraza ? truncada : null;
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
            ResultadoNeanderCompilacion neander,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            List<PasoSintactico> trazaSintactica,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean trazaSintacticaTruncada
    ) {
        @JsonProperty("estado")
        public EstadoCompilacion estado() {
            if (!exitoso) {
                if (!erroresLexicos.isEmpty()) {
                    return EstadoCompilacion.ERROR_LEXICO;
                }
                if (!erroresSintacticos.isEmpty()) {
                    return EstadoCompilacion.ERROR_SINTACTICO;
                }
                if (!erroresSemanticos.isEmpty()) {
                    return EstadoCompilacion.ERROR_SEMANTICO;
                }
                return EstadoCompilacion.ENTRADA_INVALIDA;
            }

            if (neander != null && neander.ejecutado() && neander.exitoso()) {
                return EstadoCompilacion.CODIGO_OBJETIVO_GENERADO;
            }

            return EstadoCompilacion.CODIGO_INTERMEDIO_GENERADO_CON_LIMITACION_OBJETIVO;
        }
    }

    public enum EstadoCompilacion {
        ENTRADA_INVALIDA,
        ERROR_LEXICO,
        ERROR_SINTACTICO,
        ERROR_SEMANTICO,
        CODIGO_INTERMEDIO_GENERADO_CON_LIMITACION_OBJETIVO,
        CODIGO_OBJETIVO_GENERADO
    }
}

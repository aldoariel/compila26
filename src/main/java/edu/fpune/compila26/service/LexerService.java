package edu.fpune.compila26.service;

import edu.fpune.compila26.dto.AnalisislexicoResponse;
import edu.fpune.compila26.lexico.TokenDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.lexico.Token;


import java.util.List;
import java.util.stream.Collectors;

@Service                    // ← Anotación Spring: esto es un servicio inyectable
public class LexerService {

    private final ValidadorCodigoFuente validadorCodigoFuente;

    public LexerService() {
        this(new ValidadorCodigoFuente());
    }

    @Autowired
    public LexerService(ValidadorCodigoFuente validadorCodigoFuente) {
        this.validadorCodigoFuente = validadorCodigoFuente;
    }

    // MÉTODO PRINCIPAL
    public AnalisislexicoResponse analizarCodigo(String codigoFuente) {

        validadorCodigoFuente.validarTamanio(codigoFuente);

        // 1. VALIDAR ENTRADA
        if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
            throw new IllegalArgumentException("...");
        }

        try {
            // 2. MEDIR TIEMPO
            long inicio = System.currentTimeMillis();

            // 3. CREAR LEXER Y ANALIZAR
            AnalisisLexicoFuente.Resultado resultadoLexico =
                    AnalisisLexicoFuente.analizar(codigoFuente);
            List<Token> tokens = resultadoLexico.tokens();

            // 4. CONVERTIR Token → TokenDto (para JSON)
            List<TokenDto> tokenDtos = tokens.stream()
                    .map(t -> new TokenDto(
                            t.getTipo(),
                            t.getValor(),
                            t.getLinea(),
                            t.getColumna()
                    ))
                    .collect(Collectors.toList());

            // 5. CALCULAR DURACIÓN
            long duracion = System.currentTimeMillis() - inicio;

            // 6. CREAR RESPUESTA
            return new AnalisislexicoResponse(
                    !resultadoLexico.tieneErrores(),
                    resultadoLexico.tieneErrores()
                            ? "Se encontraron errores léxicos."
                            : "Análisis léxico exitoso.",
                    tokenDtos,                      // tokens convertidos
                    tokens.size(),                  // cantidad
                    duracion                        // tiempo en ms
            );

        } catch (Exception e) {
            throw new RuntimeException("Error durante análisis léxico: " + e.getMessage());
        }
    }

    // MÉTODO AUXILIAR
    public boolean validarCodigo(String codigo) {
        return codigo != null && !codigo.trim().isEmpty();
    }
}

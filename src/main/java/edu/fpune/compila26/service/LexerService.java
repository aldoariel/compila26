package edu.fpune.compila26.service;

import edu.fpune.compila26.dto.AnalisislexicoResponse;
import edu.fpune.compila26.lexico.Lexer;
import edu.fpune.compila26.lexico.TokenDto;
import org.springframework.stereotype.Service;

import edu.fpune.compila26.lexico.Token;


import java.util.List;
import java.util.stream.Collectors;

import java.util.List;

@Service                    // ← Anotación Spring: esto es un servicio inyectable
public class LexerService {

    // MÉTODO PRINCIPAL
    public AnalisislexicoResponse analizarCodigo(String codigoFuente) {

        // 1. VALIDAR ENTRADA
        if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
            throw new IllegalArgumentException("...");
        }

        try {
            // 2. MEDIR TIEMPO
            long inicio = System.currentTimeMillis();

            // 3. CREAR LEXER Y ANALIZAR
            Lexer lexer = new Lexer();
            List<Token> tokens = lexer.analizar(codigoFuente);  // ← Tu código

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
                    true,                           // exitoso
                    "Análisis léxico exitoso...",   // mensaje
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
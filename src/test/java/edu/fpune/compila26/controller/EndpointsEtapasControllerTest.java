package edu.fpune.compila26.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.fpune.compila26.service.IntermedioService;
import edu.fpune.compila26.service.LexerService;
import edu.fpune.compila26.service.ParserService;
import edu.fpune.compila26.service.SemanticoService;

class EndpointsEtapasControllerTest {

    private static final String CODIGO_CON_ERROR_LEXICO =
            "{\"codigo\":\"int x; x = 1 @ 2;\"}";

    @Test
    void lexerConservaTokensPeroClasificaElErrorLexico() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new LexerController(new LexerService()))
                .build();

        mockMvc.perform(post("/api/lexer/analizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODIGO_CON_ERROR_LEXICO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores léxicos."))
                .andExpect(jsonPath("$.tokens[?(@.tipo == 'ERROR')]").isNotEmpty());
    }

    @Test
    void parserDetieneElAnalisisAnteErrorLexico() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ParserController(new ParserService()))
                .build();

        mockMvc.perform(post("/api/parser/analizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODIGO_CON_ERROR_LEXICO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores léxicos."))
                .andExpect(jsonPath("$.ast").doesNotExist())
                .andExpect(jsonPath("$.errores[0]").value(
                        "Error léxico: carácter no reconocido '@' en línea 1, columna 14."));
    }

    @Test
    void semanticoDetieneElAnalisisAnteErrorLexico() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new SemanticoController(new SemanticoService()))
                .build();

        mockMvc.perform(post("/api/semantico/analizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODIGO_CON_ERROR_LEXICO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores léxicos."))
                .andExpect(jsonPath("$.errores[0]").value(
                        "Error léxico: carácter no reconocido '@' en línea 1, columna 14."))
                .andExpect(jsonPath("$.simbolos").isEmpty());
    }

    @Test
    void intermedioMantieneLaMismaClasificacionLexica() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new IntermedioController(new IntermedioService()))
                .build();

        mockMvc.perform(post("/api/intermedio/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODIGO_CON_ERROR_LEXICO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(false))
                .andExpect(jsonPath("$.mensaje").value("Se encontraron errores léxicos."))
                .andExpect(jsonPath("$.errores[0]").value(
                        "Error léxico: carácter no reconocido '@' en línea 1, columna 14."))
                .andExpect(jsonPath("$.codigoIntermedio").isEmpty());
    }
}

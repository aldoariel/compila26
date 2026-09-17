package edu.fpune.compila26.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.fpune.compila26.service.CompilerService;

class CompilerControllerTest {

    @Test
    void compilarCodigoValidoDesdeEndpointHttp() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new CompilerController(new CompilerService()))
                .build();

        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x; x = 10 + 5 * 2;\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.codigoFuente").value("int x; x = 10 + 5 * 2;"))
                .andExpect(jsonPath("$.tokens.length()").value(11))
                .andExpect(jsonPath("$.ast.tipo").value("PROGRAMA"))
                .andExpect(jsonPath("$.simbolos[0].nombre").value("x"))
                .andExpect(jsonPath("$.simbolos[0].tipo").value("int"))
                .andExpect(jsonPath("$.codigoIntermedio[0]").value("t0 = 5 * 2"))
                .andExpect(jsonPath("$.codigoIntermedio[1]").value("t1 = 10 + t0"))
                .andExpect(jsonPath("$.codigoIntermedio[2]").value("x = t1"));
    }

    @Test
    void exponerResultadoNeanderDesdeElEndpointExistente() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new CompilerController(new CompilerService()))
                .build();

        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x; x = 10 + 5;\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.neander.ejecutado").value(true))
                .andExpect(jsonPath("$.neander.exitoso").value(true))
                .andExpect(jsonPath("$.neander.instrucciones[0]").value("LDA 82H"))
                .andExpect(jsonPath("$.neander.instrucciones[5]").value("HLT"))
                .andExpect(jsonPath("$.neander.datos[0].nombre").value("x"))
                .andExpect(jsonPath("$.neander.datos[0].direccion").value("80H"));
    }
}

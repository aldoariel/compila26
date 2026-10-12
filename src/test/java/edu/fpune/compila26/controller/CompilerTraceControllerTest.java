package edu.fpune.compila26.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.fpune.compila26.service.CompilerService;

class CompilerTraceControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CompilerController(new CompilerService()))
                .build();
    }

    @Test
    void conservarContratoAnteriorSinCamposDeTraza() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x;\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.trazaSintactica").doesNotExist())
                .andExpect(jsonPath("$.trazaSintacticaTruncada").doesNotExist());
    }

    @Test
    void omitirCamposCuandoIncluirTrazaEsFalso() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x;\",\"incluirTraza\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trazaSintactica").doesNotExist())
                .andExpect(jsonPath("$.trazaSintacticaTruncada").doesNotExist());
    }

    @Test
    void exponerTodosLosCamposRealesDelEventoCuandoSeSolicita() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x; x = 10 + 5 * 2;\",\"incluirTraza\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.trazaSintacticaTruncada").value(false))
                .andExpect(jsonPath("$.trazaSintactica[0].numero").value(1))
                .andExpect(jsonPath("$.trazaSintactica[0].evento").value("ENTRAR_FUNCION"))
                .andExpect(jsonPath("$.trazaSintactica[0].funcion").value("programa"))
                .andExpect(jsonPath("$.trazaSintactica[0].produccion").value("programa -> { sentencia }"))
                .andExpect(jsonPath("$.trazaSintactica[0].tokenActual").value("int"))
                .andExpect(jsonPath("$.trazaSintactica[0].tipoToken").value("PALABRA_CLAVE"))
                .andExpect(jsonPath("$.trazaSintactica[0].linea").value(1))
                .andExpect(jsonPath("$.trazaSintactica[0].columna").value(1))
                .andExpect(jsonPath("$.trazaSintactica[0].profundidad").value(0))
                .andExpect(jsonPath("$.trazaSintactica[0].descripcion").isString())
                .andExpect(jsonPath("$.trazaSintactica[0].nodoTipo").isEmpty())
                .andExpect(jsonPath("$.trazaSintactica[0].nodoValor").isEmpty())
                .andExpect(jsonPath("$.trazaSintactica[?(@.nodoValor == '*')]").isNotEmpty())
                .andExpect(jsonPath("$.trazaSintactica[?(@.nodoValor == '+')]").isNotEmpty());
    }

    @Test
    void exponerErrorYRecuperacionSintactica() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x; x = 10 + ;\",\"incluirTraza\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ERROR_SINTACTICO"))
                .andExpect(jsonPath("$.trazaSintactica[?(@.evento == 'ERROR_SINTACTICO')]").isNotEmpty())
                .andExpect(jsonPath("$.trazaSintactica[?(@.evento == 'INICIAR_RECUPERACION')]").isNotEmpty())
                .andExpect(jsonPath("$.trazaSintactica[?(@.evento == 'FINALIZAR_RECUPERACION')]").isNotEmpty());
    }

    @Test
    void devolverListaVaciaAnteErrorLexicoSinEjecutarParser() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"int x; x = 1 @ 2;\",\"incluirTraza\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ERROR_LEXICO"))
                .andExpect(jsonPath("$.trazaSintactica").isArray())
                .andExpect(jsonPath("$.trazaSintactica").isEmpty())
                .andExpect(jsonPath("$.trazaSintacticaTruncada").value(false));
    }
}

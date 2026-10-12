package edu.fpune.compila26;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class Compila26ApplicationTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void rutaPrincipalDevuelveLaInterfaz() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andExpect(content().string(containsString("<title>Compila26")))
				.andExpect(content().string(containsString("id=\"compile-form\"")));
	}

	@Test
	void indexYRecursosEstaticosContinuanAccesibles() throws Exception {
		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"));

		mockMvc.perform(get("/css/compila26.css"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/css"));

		mockMvc.perform(get("/js/compila26.js"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/javascript"));
	}

	@Test
	void todosLosEndpointsRechazanCodigoExcesivamenteGrande() throws Exception {
		String codigo = "a".repeat(20_001);
		String cuerpo = "{\"codigo\":\"" + codigo + "\"}";
		String[] rutas = {
				"/api/lexer/analizar",
				"/api/parser/analizar",
				"/api/semantico/analizar",
				"/api/intermedio/generar",
				"/api/compiler/compile"
		};

		for (String ruta : rutas) {
			mockMvc.perform(post(ruta)
						.contentType(MediaType.APPLICATION_JSON)
						.content(cuerpo))
					.andExpect(status().isContentTooLarge())
					.andExpect(jsonPath("$.codigo_error").value("CODIGO_DEMASIADO_GRANDE"))
					.andExpect(jsonPath("$.detalles").value(
							"Caracteres recibidos: 20001; máximo permitido: 20000."));
		}
	}

}

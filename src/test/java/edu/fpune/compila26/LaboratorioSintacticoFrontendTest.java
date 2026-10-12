package edu.fpune.compila26;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LaboratorioSintacticoFrontendTest {

    private static String html;
    private static String javascript;
    private static String css;

    @BeforeAll
    static void cargarRecursos() throws IOException {
        html = recurso("static/index.html");
        javascript = recurso("static/js/compila26.js");
        css = recurso("static/css/compila26.css");
    }

    @Test
    void activarTrazaSoloParaElLaboratorio() {
        assertTrue(html.contains("id=\"syntax-lab-enabled\""));
        assertTrue(javascript.contains("if (syntaxLabEnabled.checked)"));
        assertTrue(javascript.contains("request.incluirTraza = true"));
        assertFalse(javascript.contains("const request = {codigo: sourceCode.value, incluirTraza: true}"));
    }

    @Test
    void incluirControlesAccesiblesDeNavegacionYReproduccion() {
        for (String id : new String[] {"lab-previous", "lab-next", "lab-play", "lab-pause", "lab-reset"}) {
            assertTrue(html.contains("id=\"" + id + "\""));
        }
        assertTrue(html.contains("aria-label=\"Controles de reproducción de la traza\""));
        assertTrue(html.contains("aria-live=\"polite\""));
        assertTrue(html.contains("role=\"tablist\""));
        assertTrue(html.contains("id=\"syntax-lab-tab\" type=\"button\" role=\"tab\""));
        assertTrue(html.contains("role=\"tabpanel\" aria-labelledby=\"syntax-lab-tab\""));
        assertTrue(javascript.contains("function showTraceStep(index)"));
    }

    @Test
    void reproducirPausarYEvitarTemporizadoresSimultaneos() {
        assertTrue(javascript.contains("syntaxTraceTimer !== null"));
        assertTrue(javascript.contains("window.setInterval"));
        assertTrue(javascript.contains("window.clearInterval(syntaxTraceTimer)"));
        assertTrue(javascript.contains("syntaxTraceTimer = null"));
        assertTrue(javascript.contains("pauseSyntaxTrace();\n    resetSyntaxLab"));
    }

    @Test
    void limitarIndiceYDetenerseEnElUltimoPaso() {
        assertTrue(javascript.contains("Math.max(0, Math.min(index, syntaxTrace.length - 1))"));
        assertTrue(javascript.contains("syntaxTraceIndex >= syntaxTrace.length - 1"));
        assertTrue(javascript.contains("labNext.disabled"));
    }

    @Test
    void reiniciarLaVisualizacionSinSolicitarNuevamenteLaApi() {
        assertTrue(javascript.contains("function resetSyntaxPlayback()"));
        assertTrue(javascript.contains("syntaxTraceIndex = -1"));
        assertTrue(javascript.contains("Paso 0 / ${syntaxTrace.length}"));
    }

    @Test
    void explicarTrazaVaciaErrorSintacticoYTruncamiento() {
        assertTrue(javascript.contains("El lexer encontró errores"));
        assertTrue(javascript.contains("La entrada está vacía"));
        assertTrue(javascript.contains("AST parcial generado durante un análisis con errores"));
        assertTrue(javascript.contains("La traza alcanzó el límite de eventos"));
    }

    @Test
    void presentarCodigoFuenteSinInyectarloComoHtml() {
        int inicio = javascript.indexOf("function renderLabSource(step)");
        int fin = javascript.indexOf("function renderLabAst", inicio);
        String funcion = javascript.substring(inicio, fin);

        assertTrue(funcion.contains("document.createTextNode"));
        assertTrue(funcion.contains("highlight.textContent"));
        assertFalse(funcion.contains("innerHTML"));
        assertTrue(funcion.contains("line.slice(columnIndex, columnIndex + token.length) !== token"));
    }

    @Test
    void mostrarProduccionFuncionProfundidadYNodoConstruido() {
        assertTrue(html.contains("id=\"lab-production\""));
        assertTrue(html.contains("id=\"lab-function\""));
        assertTrue(html.contains("id=\"lab-built-node\""));
        assertTrue(javascript.contains("step.profundidad"));
        assertTrue(javascript.contains("[\"Profundidad de llamadas\", step.profundidad]"));
        assertTrue(javascript.contains("[\"Paso\", step.numero]"));
        assertTrue(javascript.contains("step.evento === \"CONSTRUIR_NODO\""));
    }

    @Test
    void ofrecerLosCincoEjemplosDidacticos() {
        for (String nombre : new String[] {"Declaración", "Asignación", "Precedencia", "Paréntesis", "Error sintáctico"}) {
            assertTrue(html.contains(">" + nombre + "</button>"));
        }
    }

    @Test
    void adaptarLaboratorioYRespetarMovimientoReducido() {
        assertTrue(css.contains(".syntax-lab-grid"));
        assertTrue(css.contains("@media (max-width: 600px)"));
        assertTrue(css.contains("@media (prefers-reduced-motion: reduce)"));
        assertTrue(javascript.contains("prefers-reduced-motion: reduce"));
    }

    private static String recurso(String nombre) throws IOException {
        try (InputStream entrada = LaboratorioSintacticoFrontendTest.class
                .getClassLoader().getResourceAsStream(nombre)) {
            if (entrada == null) {
                throw new IOException("No se encontró el recurso " + nombre);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

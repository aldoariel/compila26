package edu.fpune.compila26.sintactico;

import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.ANTICIPACION;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.CONSTRUIR_NODO;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.CONSUMIR_TOKEN;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.ERROR_SINTACTICO;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.FINALIZAR_RECUPERACION;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.INICIAR_RECUPERACION;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.SALIR_FUNCION;
import static edu.fpune.compila26.sintactico.PasoSintactico.Evento.SELECCIONAR_PRODUCCION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.fpune.compila26.lexico.Lexer;

class ParserTrazaTest {

    @Test
    void noGenerarTrazaCuandoNoFueSolicitada() {
        Parser parser = parser("int x;", false);
        parser.parse();

        assertTrue(parser.getTraza().isEmpty());
        assertFalse(parser.isTrazaTruncada());
    }

    @Test
    void registrarEntradaSalidaProduccionesAnticipacionYConsumoEnOrden() {
        Parser parser = parser("int x;", true);
        parser.parse();
        List<PasoSintactico> pasos = parser.getTraza();

        assertEquals(PasoSintactico.Evento.ENTRAR_FUNCION, pasos.get(0).evento());
        assertEquals("programa", pasos.get(0).funcion());
        assertEquals(SALIR_FUNCION, pasos.get(pasos.size() - 1).evento());
        assertEquals("programa", pasos.get(pasos.size() - 1).funcion());
        assertTrue(pasos.stream().anyMatch(p -> p.evento() == ANTICIPACION && p.tokenActual().equals("int")));
        assertTrue(pasos.stream().anyMatch(p -> p.evento() == SELECCIONAR_PRODUCCION
                && "sentencia -> declaracion".equals(p.produccion())));
        assertEquals(List.of("int", "x", ";"), pasos.stream()
                .filter(p -> p.evento() == CONSUMIR_TOKEN).map(PasoSintactico::tokenActual).toList());
        assertEquals(pasos.size(), pasos.get(pasos.size() - 1).numero());
    }

    @Test
    void representarLaProfundidadRealDeLasLlamadas() {
        Parser parser = parser("x = 1;", true);
        parser.parse();

        assertEquals(0, entrada(parser, "programa").profundidad());
        assertEquals(1, entrada(parser, "sentencia").profundidad());
        assertEquals(2, entrada(parser, "asignacion").profundidad());
        assertEquals(3, entrada(parser, "expresion").profundidad());
        assertEquals(6, entrada(parser, "factor").profundidad());
    }

    @Test
    void construirMultiplicacionAntesQueSuma() {
        Parser parser = parser("x = 10 + 5 * 2;", true);
        parser.parse();

        int multiplicacion = indiceNodoOperador(parser, "*");
        int suma = indiceNodoOperador(parser, "+");
        assertTrue(multiplicacion < suma);
        assertTrue(parser.getTraza().stream().anyMatch(p -> p.evento() == SELECCIONAR_PRODUCCION
                && p.produccion().startsWith("termino -> termino") && "*".equals(p.tokenActual())));
    }

    @Test
    void registrarLaProduccionDeFactorConParentesis() {
        Parser parser = parser("x = (10 + 5) * 2;", true);
        parser.parse();

        assertTrue(parser.getTraza().stream().anyMatch(p -> p.evento() == SELECCIONAR_PRODUCCION
                && "factor -> '(' expresion ')'".equals(p.produccion()) && "(".equals(p.tokenActual())));
    }

    @Test
    void registrarErrorYRecuperacionConSuUbicacion() {
        Parser parser = parser("int x;\nx = 10 + ;", true);
        parser.parse();

        PasoSintactico error = parser.getTraza().stream()
                .filter(p -> p.evento() == ERROR_SINTACTICO).findFirst().orElseThrow();
        assertEquals(";", error.tokenActual());
        assertEquals(2, error.linea());
        assertEquals(10, error.columna());
        assertTrue(parser.getTraza().stream().anyMatch(p -> p.evento() == INICIAR_RECUPERACION));
        assertTrue(parser.getTraza().stream().anyMatch(p -> p.evento() == FINALIZAR_RECUPERACION));
    }

    @Test
    void trazarProgramaVacio() {
        Parser parser = parser("", true);
        NodoAST ast = parser.parse();

        assertEquals("PROGRAMA", ast.getTipo());
        assertTrue(parser.getTraza().stream().anyMatch(p -> p.evento() == CONSTRUIR_NODO
                && "PROGRAMA".equals(p.nodoTipo())));
        assertEquals(SALIR_FUNCION, parser.getTraza().get(parser.getTraza().size() - 1).evento());
    }

    @Test
    void respetarElLimiteSinInterrumpirElParser() {
        Parser parser = new Parser(new Lexer().analizar("int x; x = 10 + 5 * 2;"), true, 7);
        NodoAST ast = parser.parse();

        assertEquals(7, parser.getTraza().size());
        assertTrue(parser.isTrazaTruncada());
        assertTrue(parser.esExitoso());
        assertEquals(2, ast.getHijos().size());
        assertThrows(UnsupportedOperationException.class,
                () -> parser.getTraza().add(parser.getTraza().get(0)));
    }

    @Test
    void mantenerEstadoAisladoEntreInstancias() {
        Parser primero = parser("int x;", true);
        Parser segundo = parser("y = 2;", true);
        primero.parse();
        segundo.parse();

        assertNotSame(primero.getTraza(), segundo.getTraza());
        assertTrue(primero.getTraza().stream().anyMatch(p -> "int".equals(p.tokenActual())));
        assertFalse(segundo.getTraza().stream().anyMatch(p -> "int".equals(p.tokenActual())));
        assertEquals(1, primero.getTraza().get(0).numero());
        assertEquals(1, segundo.getTraza().get(0).numero());
    }

    @Test
    void activarTrazaNoModificarAstNiErrores() {
        String codigo = "int x; x = 10 + 5 * 2; z = 1 + ;";
        Parser sinTraza = parser(codigo, false);
        Parser conTraza = parser(codigo, true);

        NodoAST astSinTraza = sinTraza.parse();
        NodoAST astConTraza = conTraza.parse();

        assertEquals(astSinTraza.toString(), astConTraza.toString());
        assertEquals(sinTraza.getErrores(), conTraza.getErrores());
    }

    @Test
    void rechazarLimiteNoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser(new Lexer().analizar(""), true, 0));
    }

    private Parser parser(String codigo, boolean incluirTraza) {
        return new Parser(new Lexer().analizar(codigo), incluirTraza);
    }

    private PasoSintactico entrada(Parser parser, String funcion) {
        return parser.getTraza().stream()
                .filter(p -> p.evento() == PasoSintactico.Evento.ENTRAR_FUNCION && funcion.equals(p.funcion()))
                .findFirst().orElseThrow();
    }

    private int indiceNodoOperador(Parser parser, String operador) {
        for (int i = 0; i < parser.getTraza().size(); i++) {
            PasoSintactico paso = parser.getTraza().get(i);
            if (paso.evento() == CONSTRUIR_NODO && "OPERADOR".equals(paso.nodoTipo())
                    && operador.equals(paso.nodoValor())) {
                return i;
            }
        }
        throw new AssertionError("No se encontro el operador " + operador);
    }
}

package edu.fpune.compila26.controller;

import edu.fpune.compila26.dto.AnalisislexicoResponse;
import edu.fpune.compila26.dto.ErrorResponse;
import edu.fpune.compila26.service.LexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController                         // ← Anotación: esto maneja requests HTTP
@RequestMapping("/api/lexer")           // ← Prefijo de URL base
@CrossOrigin(origins = "*")             // ← Permite acceso desde cualquier origen
public class LexerController {

    @Autowired                          // ← Inyección de dependencia
    private LexerService lexerService;  // ← Usa LexerService

    // ENDPOINT 1: POST /api/lexer/analizar
    @PostMapping("/analizar")           // ← URL completa: /api/lexer/analizar
    public ResponseEntity<?> analizar(@RequestBody Map<String, String> request) {
        try {
            // 1. EXTRAER PARÁMETRO
            String codigoFuente = request.get("codigo");

            // 2. VALIDAR
            if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()              // ← HTTP 400
                        .body(new ErrorResponse(
                                "CODIGO_VACIO",
                                "El código fuente no puede estar vacío",
                                "Proporciona código en el campo 'codigo'"
                        ));
            }

            // 3. LLAMAR AL SERVICIO
            AnalisislexicoResponse respuesta = lexerService.analizarCodigo(codigoFuente);

            // 4. RETORNAR RESPUESTA EXITOSA
            return ResponseEntity.ok(respuesta);  // ← HTTP 200 + JSON

        } catch (IllegalArgumentException e) {
            // 5. MANEJO DE ERROR: ArgumentoInválido
            return ResponseEntity
                    .badRequest()              // ← HTTP 400
                    .body(new ErrorResponse(
                            "CODIGO_VACIO",
                            "El código fuente no puede estar vacío",
                            "Proporciona código en el campo 'codigo'"
                    ));

        } catch (Exception e) {
            // 6. MANEJO DE ERROR: Error Interno
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)  // ← HTTP 500
                    .body(new ErrorResponse(
                            "CODIGO_VACIO",
                            "El código fuente no puede estar vacío",
                            "Proporciona código en el campo 'codigo'"
                    ));
        }
    }

    // ENDPOINT 2: GET /api/lexer/health
    @GetMapping("/health")              // ← URL completa: /api/lexer/health
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("servicio", "Analizador Léxico");
        status.put("estado", "activo");
        status.put("version", "2.0.0");
        status.put("java", System.getProperty("java.version"));
        return ResponseEntity.ok(status);  // ← HTTP 200 + JSON
    }
}
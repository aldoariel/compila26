package edu.fpune.compila26.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.fpune.compila26.service.SemanticoService;

@RestController
@RequestMapping("/api/semantico")
public class SemanticoController {

    private final SemanticoService semanticoService;

    public SemanticoController(
            SemanticoService semanticoService) {

        this.semanticoService = semanticoService;
    }

    @PostMapping("/analizar")
    public ResponseEntity<SemanticoService.ResultadoSemantico> analizar(
            @RequestBody CodigoRequest request) {

        String codigo =
                request == null
                        ? null
                        : request.codigo();

        SemanticoService.ResultadoSemantico resultado =
                semanticoService.analizar(codigo);

        return ResponseEntity.ok(resultado);
    }

    /**
     * JSON recibido.
     *
     * {
     *     "codigo": "x = 10;"
     * }
     */
    public record CodigoRequest(
            String codigo
    ) {
    }
}
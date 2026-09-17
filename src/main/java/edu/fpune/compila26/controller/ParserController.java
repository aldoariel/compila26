package edu.fpune.compila26.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.fpune.compila26.service.ParserService;

@RestController
@RequestMapping("/api/parser")
public class ParserController {

    private final ParserService parserService;

    public ParserController(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/analizar")
    public ResponseEntity<ParserService.ResultadoParser> analizar(
            @RequestBody CodigoRequest request) {

        ParserService.ResultadoParser resultado =
                parserService.analizar(request.codigo());

        return ResponseEntity.ok(resultado);
    }

    public record CodigoRequest(String codigo) {
    }
}
package edu.fpune.compila26.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fpune.compila26.service.IntermedioService;

@RestController
@RequestMapping("/api/intermedio")
public class IntermedioController {

    private final IntermedioService intermedioService;

    public IntermedioController(IntermedioService intermedioService) {
        this.intermedioService = intermedioService;
    }

    @PostMapping("/generar")
    public ResponseEntity<IntermedioService.ResultadoIntermedio> generar(
            @RequestBody CodigoRequest request) {

        String codigo = request == null ? null : request.codigo();

        return ResponseEntity.ok(intermedioService.generar(codigo));
    }

    public record CodigoRequest(String codigo) {
    }
}

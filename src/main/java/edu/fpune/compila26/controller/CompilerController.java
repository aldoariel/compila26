package edu.fpune.compila26.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.fpune.compila26.service.CompilerService;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final CompilerService compilerService;

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping("/compile")
    public ResponseEntity<CompilerService.ResultadoCompilacion> compilar(
            @RequestBody CodigoRequest request) {

        String codigo = request == null ? null : request.codigo();

        return ResponseEntity.ok(compilerService.compilar(codigo));
    }

    public record CodigoRequest(String codigo) {
    }
}

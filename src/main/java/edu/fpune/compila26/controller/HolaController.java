package edu.fpune.compila26.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Resource inicio() {
        return new ClassPathResource("static/index.html");
    }
}

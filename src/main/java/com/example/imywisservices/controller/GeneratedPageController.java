package com.example.imywisservices.controller;

import com.example.imywisservices.service.GraphHtmlService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeneratedPageController {

    private final GraphHtmlService graphHtmlService;

    public GeneratedPageController(GraphHtmlService graphHtmlService) {
        this.graphHtmlService = graphHtmlService;
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getGeneratedPage() {
        Path latest = graphHtmlService.getLastGeneratedFile();
        if (latest == null || !Files.exists(latest)) {
            return ResponseEntity.notFound().build();
        }
        try {
            String html = Files.readString(latest, StandardCharsets.UTF_8);
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

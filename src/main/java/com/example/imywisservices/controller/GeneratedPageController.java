package com.example.imywisservices.controller;

import com.example.imywisservices.service.GraphHtmlService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeneratedPageController {

    private final GraphHtmlService graphHtmlService;

    public GeneratedPageController(GraphHtmlService graphHtmlService) {
        this.graphHtmlService = graphHtmlService;
    }

    @GetMapping(value = {"/{userHandle}", "/{userHandle}/{pageName:.+\\.html}"},
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getGeneratedPage(
            @PathVariable(name = "userHandle") String userHandle,
            @PathVariable(name = "pageName", required = false) String pageName) {
        Path generatedPagesDir = graphHtmlService.getGeneratedPagesDir(userHandle);
        String targetName = (pageName == null || pageName.isBlank()) ? "index.html" : pageName.trim();
        Path requestedPath = generatedPagesDir.resolve(targetName).normalize();

        // Prevent resolving files outside the generated pages directory.
        if (!requestedPath.startsWith(generatedPagesDir.normalize()) || !Files.exists(requestedPath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            String html = Files.readString(requestedPath, StandardCharsets.UTF_8);
            return ResponseEntity.ok(html);
        } catch (Exception e) {
            System.err.println("Error reading generated page: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLandingMessage() {
        return ResponseEntity.ok("I'll miss you when I scroll");
    }
}

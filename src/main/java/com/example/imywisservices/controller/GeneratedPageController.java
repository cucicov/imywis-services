package com.example.imywisservices.controller;

import com.example.imywisservices.service.GraphHtmlService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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

    @GetMapping(value = {"/img/{userHandle}/{assetName:.+}", "/{userHandle}/img/{assetName:.+}"})
    public ResponseEntity<byte[]> getGeneratedImageAsset(
            @PathVariable(name = "userHandle") String userHandle,
            @PathVariable(name = "assetName") String assetName) {
        Path generatedPagesDir = graphHtmlService.getGeneratedPagesDir(userHandle);
        Path requestedPath = generatedPagesDir.resolve("img").resolve(assetName).normalize();

        // Prevent resolving files outside the generated user asset directory.
        if (!requestedPath.startsWith(generatedPagesDir.normalize()) || !Files.exists(requestedPath) || Files.isDirectory(requestedPath)) {
            return ResponseEntity.notFound().build();
        }

        String lowerName = requestedPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(requestedPath);
            String detectedType = Files.probeContentType(requestedPath);
            MediaType mediaType = (detectedType == null || detectedType.isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(detectedType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading generated image asset: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/img/{assetName:.+}")
    public ResponseEntity<byte[]> getGeneratedRootImageAsset(
            @PathVariable(name = "assetName") String assetName) {
        Path generatedPagesDir = graphHtmlService.getGeneratedPagesDir();
        Path requestedPath = generatedPagesDir.resolve("img").resolve(assetName).normalize();

        // Prevent resolving files outside the generated root asset directory.
        if (!requestedPath.startsWith(generatedPagesDir.normalize()) || !Files.exists(requestedPath) || Files.isDirectory(requestedPath)) {
            return ResponseEntity.notFound().build();
        }

        String lowerName = requestedPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(requestedPath);
            String detectedType = Files.probeContentType(requestedPath);
            MediaType mediaType = (detectedType == null || detectedType.isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(detectedType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading generated root image asset: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = {"/fonts/{userHandle}/{assetName:.+}", "/{userHandle}/fonts/{assetName:.+}"})
    public ResponseEntity<byte[]> getGeneratedFontAsset(
            @PathVariable(name = "userHandle") String userHandle,
            @PathVariable(name = "assetName") String assetName) {
        Path generatedPagesDir = graphHtmlService.getGeneratedPagesDir(userHandle);
        Path requestedPath = generatedPagesDir.resolve("fonts").resolve(assetName).normalize();

        // Prevent resolving files outside the generated user font directory.
        if (!requestedPath.startsWith(generatedPagesDir.normalize()) || !Files.exists(requestedPath) || Files.isDirectory(requestedPath)) {
            return ResponseEntity.notFound().build();
        }

        String lowerName = requestedPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(requestedPath);
            String detectedType = Files.probeContentType(requestedPath);
            MediaType mediaType = (detectedType == null || detectedType.isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(detectedType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading generated font asset: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/fonts/{assetName:.+}")
    public ResponseEntity<byte[]> getGeneratedRootFontAsset(
            @PathVariable(name = "assetName") String assetName) {
        Path generatedPagesDir = graphHtmlService.getGeneratedPagesDir();
        Path requestedPath = generatedPagesDir.resolve("fonts").resolve(assetName).normalize();

        // Prevent resolving files outside the generated root font directory.
        if (!requestedPath.startsWith(generatedPagesDir.normalize()) || !Files.exists(requestedPath) || Files.isDirectory(requestedPath)) {
            return ResponseEntity.notFound().build();
        }

        String lowerName = requestedPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(requestedPath);
            String detectedType = Files.probeContentType(requestedPath);
            MediaType mediaType = (detectedType == null || detectedType.isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(detectedType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading generated root font asset: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/favicon.ico")
    public ResponseEntity<byte[]> getFavicon() {
        try {
            Path faviconPath = Path.of("src/main/resources/static/favicon.ico");
            if (!Files.exists(faviconPath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] content = Files.readAllBytes(faviconPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/x-icon"))
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading favicon: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLandingMessage() {
        return ResponseEntity.ok("I'll miss you when I scroll");
    }
}

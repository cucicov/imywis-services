package com.example.imywisservices.controller;

import com.example.imywisservices.service.GraphHtmlService;
import com.example.imywisservices.service.UserProfileService;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeneratedPageController {

    private final GraphHtmlService graphHtmlService;
    private final UserProfileService userProfileService;

    public GeneratedPageController(GraphHtmlService graphHtmlService, UserProfileService userProfileService) {
        this.graphHtmlService = graphHtmlService;
        this.userProfileService = userProfileService;
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
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Cache-Control", "public, max-age=31536000")
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
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Cache-Control", "public, max-age=31536000")
                    .body(content);
        } catch (Exception e) {
            System.err.println("Error reading generated root font asset: " + requestedPath);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/favicon.svg")
    public ResponseEntity<byte[]> getFavicon() {
        try {
            // Try multiple possible paths
            ClassPathResource resource = new ClassPathResource("/static/favicon.svg");
            if (!resource.exists()) {
                resource = new ClassPathResource("static/favicon.svg");
            }
            if (!resource.exists()) {
                resource = new ClassPathResource("/favicon.svg");
            }

            System.out.println("Attempting to load favicon.svg, path: " + resource.getPath() + ", exists: " + resource.exists());

            if (!resource.exists()) {
                System.err.println("favicon.svg not found in any classpath location");
                return ResponseEntity.notFound().build();
            }

            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                System.out.println("Successfully loaded favicon.svg, size: " + content.length + " bytes");
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/svg+xml"))
                        .header("Cache-Control", "public, max-age=86400")
                        .body(content);
            }
        } catch (Exception e) {
            System.err.println("Error reading favicon.svg: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/favicon.ico")
    public ResponseEntity<byte[]> getFaviconIco() {
        try {
            // Try multiple possible paths
            ClassPathResource resource = new ClassPathResource("/static/favicon.ico");
            if (!resource.exists()) {
                resource = new ClassPathResource("static/favicon.ico");
            }
            if (!resource.exists()) {
                resource = new ClassPathResource("/favicon.ico");
            }

            System.out.println("Attempting to load favicon.ico, path: " + resource.getPath() + ", exists: " + resource.exists());

            if (!resource.exists()) {
                System.err.println("favicon.ico not found in any classpath location");
                return ResponseEntity.notFound().build();
            }

            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                System.out.println("Successfully loaded favicon.ico, size: " + content.length + " bytes");
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("image/x-icon"))
                        .header("Cache-Control", "public, max-age=86400")
                        .body(content);
            }
        } catch (Exception e) {
            System.err.println("Error reading favicon.ico: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/logo-big.png")
    public ResponseEntity<byte[]> getLogoBig() {
        try {
            // Try multiple possible paths
            ClassPathResource resource = new ClassPathResource("/static/logo-big.png");
            if (!resource.exists()) {
                resource = new ClassPathResource("static/logo-big.png");
            }
            if (!resource.exists()) {
                resource = new ClassPathResource("/logo-big.png");
            }

            System.out.println("Attempting to load logo-big.png, path: " + resource.getPath() + ", exists: " + resource.exists());

            if (!resource.exists()) {
                System.err.println("logo-big.png not found in any classpath location");
                return ResponseEntity.notFound().build();
            }

            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                System.out.println("Successfully loaded logo-big.png, size: " + content.length + " bytes");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header("Cache-Control", "public, max-age=86400")
                        .body(content);
            }
        } catch (Exception e) {
            System.err.println("Error reading logo-big.png: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/fav.png")
    public ResponseEntity<byte[]> getFaviconPng() {
        try {
            // Try multiple possible paths
            ClassPathResource resource = new ClassPathResource("/static/fav.png");
            if (!resource.exists()) {
                resource = new ClassPathResource("static/fav.png");
            }
            if (!resource.exists()) {
                resource = new ClassPathResource("/fav.png");
            }

            System.out.println("Attempting to load fav.png, path: " + resource.getPath() + ", exists: " + resource.exists());

            if (!resource.exists()) {
                System.err.println("fav.png not found in any classpath location");
                return ResponseEntity.notFound().build();
            }

            try (InputStream is = resource.getInputStream()) {
                byte[] content = is.readAllBytes();
                System.out.println("Successfully loaded fav.png, size: " + content.length + " bytes");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header("Cache-Control", "public, max-age=86400")
                        .body(content);
            }
        } catch (Exception e) {
            System.err.println("Error reading fav.png: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/")
    public ResponseEntity<Void> getLandingMessage() {
        String mainPageHandle = userProfileService.getMainPageHandle();
        if (mainPageHandle != null && !mainPageHandle.isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/" + mainPageHandle)
                    .build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/cucicov")
                .build();
    }
}

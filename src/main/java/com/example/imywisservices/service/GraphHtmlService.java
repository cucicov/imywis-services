package com.example.imywisservices.service;

import com.example.imywisservices.dto.GraphDTO;
import com.example.imywisservices.dto.MetadataDTO;
import com.example.imywisservices.dto.NodeDTO;
import com.example.imywisservices.dto.NodeDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class GraphHtmlService {

    private static final int DEFAULT_CANVAS_WIDTH = 800;
    private static final int DEFAULT_CANVAS_HEIGHT = 600;
    private static final String OUTPUT_DIR_NAME = "generated-pages";
    private static final String PAGE_NODE_TYPE = "pageNode";
    private static final String IMAGE_NODE_TYPE = "imageNode";

    private final AtomicReference<Path> lastGeneratedFile = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void generatePages(GraphDTO graph) {
        if (graph == null || graph.getNodes() == null) {
            return;
        }

        for (NodeDTO node : graph.getNodes()) {
            if (node == null || !PAGE_NODE_TYPE.equals(node.getType())) {
                continue;
            }
            Path generated = generatePage(node);
            if (generated != null) {
                lastGeneratedFile.set(generated);
            }
        }
    }

    public Path getLastGeneratedFile() {
        Path cached = lastGeneratedFile.get();
        if (cached != null && Files.exists(cached)) {
            return cached;
        }
        return findMostRecentGeneratedFile();
    }

    private Path generatePage(NodeDTO pageNode) {
        NodeDataDTO data = pageNode.getData();
        if (data == null || data.getName() == null || data.getName().isBlank()) {
            return null;
        }

        int canvasWidth = data.getWidth() != null ? data.getWidth() : DEFAULT_CANVAS_WIDTH;
        int canvasHeight = data.getHeight() != null ? data.getHeight() : DEFAULT_CANVAS_HEIGHT;

        String fileName = normalizeFileName(data.getName());
        Path outputDir = Paths.get(OUTPUT_DIR_NAME);
        Path outputFile = outputDir.resolve(fileName);

        List<ImageNodePayload> images = extractImageNodes(data.getMetadata());
        String imagesJson = toJson(images);

        String html = buildHtml(canvasWidth, canvasHeight, data.getMousePointer(), imagesJson);

        try {
            Files.createDirectories(outputDir);
            Files.writeString(outputFile, html, StandardCharsets.UTF_8);
            return outputFile;
        } catch (Exception e) {
            return null;
        }
    }

    private Path findMostRecentGeneratedFile() {
        Path outputDir = Paths.get(OUTPUT_DIR_NAME);
        if (!Files.isDirectory(outputDir)) {
            return null;
        }
        try {
            return Files.list(outputDir)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".html"))
                    .max((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private List<ImageNodePayload> extractImageNodes(MetadataDTO metadata) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return Collections.emptyList();
        }
        List<ImageNodePayload> images = new ArrayList<>();
        for (NodeDTO node : metadata.getSourceNodes()) {
            if (node == null || !IMAGE_NODE_TYPE.equals(node.getType())) {
                continue;
            }
            NodeDataDTO data = node.getData();
            if (data == null || data.getPath() == null || data.getPath().isBlank()) {
                continue;
            }
            images.add(new ImageNodePayload(
                    data.getPath(),
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    data.getWidth(),
                    data.getHeight(),
                    Boolean.TRUE.equals(data.getAutoWidth()),
                    Boolean.TRUE.equals(data.getAutoHeight()),
                    data.getOpacity() != null ? data.getOpacity() : 1.0
            ));
        }
        return images;
    }

    private String buildHtml(int canvasWidth, int canvasHeight, String mousePointer, String imagesJson) {
        String safeMousePointer = mousePointer == null ? "" : mousePointer;
        String template = """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1"/>
                    <title>Generated Page</title>
                    <style>
                      html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #ffffff; }
                      canvas { display: block; }
                    </style>
                    <script src="https://cdn.jsdelivr.net/npm/p5@1.9.0/lib/p5.min.js"></script>
                  </head>
                  <body>
                    <script>
                      const IMAGE_NODES = __IMAGE_NODES__;
                      const MOUSE_POINTER_SRC = __MOUSE_POINTER_SRC__;
                      const CANVAS_W = __CANVAS_W__;
                      const CANVAS_H = __CANVAS_H__;
                
                      let images = [];
                      let mousePointerImg = null;
                
                      function toProxyUrl(url) {
                        if (!url) return url;
                        const trimmed = url.trim();
                        if (!trimmed) return trimmed;
                        const lower = trimmed.toLowerCase();
                        if (lower.startsWith("http://") || lower.startsWith("https://")) {
                          return "https://corsproxy.io/?" + encodeURIComponent(trimmed);
                        }
                        return trimmed;
                      }
                
                      function preload() {
                        images = IMAGE_NODES.map(node => ({
                          node,
                          img: loadImage(toProxyUrl(node.src))
                        }));
                        if (MOUSE_POINTER_SRC) {
                          mousePointerImg = loadImage(toProxyUrl(MOUSE_POINTER_SRC));
                        }
                      }
                
                      function setup() {
                        createCanvas(CANVAS_W, CANVAS_H);
                        if (MOUSE_POINTER_SRC) {
                          noCursor();
                        } else {
                          cursor();
                        }
                      }
                
                      function draw() {
                        clear();
                        for (const entry of images) {
                          const node = entry.node;
                          const img = entry.img;
                          const w = node.autoWidth ? img.width : node.width;
                          const h = node.autoHeight ? img.height : node.height;
                          const opacity = Math.max(0, Math.min(1, node.opacity));
                          tint(255, opacity * 255);
                          image(img, node.x, node.y, w, h);
                        }
                        noTint();
                        if (mousePointerImg) {
                          image(mousePointerImg, mouseX, mouseY);
                        }
                      }
                    </script>
                  </body>
                </html>
                """;
        return template
                .replace("__IMAGE_NODES__", imagesJson)
                .replace("__MOUSE_POINTER_SRC__", toJsonValue(safeMousePointer))
                .replace("__CANVAS_W__", String.valueOf(canvasWidth))
                .replace("__CANVAS_H__", String.valueOf(canvasHeight));
    }

    private String toJson(List<ImageNodePayload> images) {
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String toJsonValue(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "\"\"";
        }
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private String normalizeFileName(String name) {
        String trimmed = name.trim();
        String base = trimmed.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.isBlank()) {
            base = "generated";
        }
        if (!base.toLowerCase(Locale.ROOT).endsWith(".html")) {
            base = base + ".html";
        }
        return base;
    }

    private static class ImageNodePayload {
        public final String src;
        public final int x;
        public final int y;
        public final Integer width;
        public final Integer height;
        public final boolean autoWidth;
        public final boolean autoHeight;
        public final double opacity;

        private ImageNodePayload(String src,
                                 int x,
                                 int y,
                                 Integer width,
                                 Integer height,
                                 boolean autoWidth,
                                 boolean autoHeight,
                                 double opacity) {
            this.src = Objects.requireNonNull(src);
            this.x = x;
            this.y = y;
            this.width = width != null ? width : 0;
            this.height = height != null ? height : 0;
            this.autoWidth = autoWidth;
            this.autoHeight = autoHeight;
            this.opacity = opacity;
        }
    }
}

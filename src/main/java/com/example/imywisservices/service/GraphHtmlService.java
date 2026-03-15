package com.example.imywisservices.service;

import com.example.imywisservices.dto.*;
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
    private static final String OUTPUT_DIR_ENV = "GENERATED_PAGES_DIR";
    private static final String PAGE_NODE_TYPE = "pageNode";
    private static final String IMAGE_NODE_TYPE = "imageNode";
    private static final String BACKGROUND_NODE_TYPE = "backgroundNode";
    private static final String TEXT_NODE_TYPE = "textNode";
    private static final String TILE_STYLE = "tile";

    private final AtomicReference<Path> lastGeneratedFile = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void generatePages(GraphDTO graph) throws Exception {
        if (graph == null || graph.getNodes() == null) {
            return;
        }

        if (graph.getNodes().isEmpty()) {
            throw new Exception("No nodes found in the graph");
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

    private Path generatePage(NodeDTO pageNode) throws Exception {
        NodeDataDTO data = pageNode.getData();
        if (data == null || data.getName() == null || data.getName().isBlank()) {
            return null;
        }

        int canvasWidth = data.getWidth() != null ? data.getWidth() : DEFAULT_CANVAS_WIDTH;
        int canvasHeight = data.getHeight() != null ? data.getHeight() : DEFAULT_CANVAS_HEIGHT;

        String fileName = normalizeFileName(data.getName());
        Path outputDir = getOutputDir();
        Path outputFile = outputDir.resolve(fileName);

        List<BackgroundNodePayload> backgrounds = extractBackgroundNodes(data.getMetadata(), canvasWidth, canvasHeight);
        List<ImageNodePayload> images = extractImageNodes(data.getMetadata());
        List<TextNodePayload> texts = extractTextNodes(data.getMetadata());

        String html = buildHtml(
                canvasWidth,
                canvasHeight,
                data.getBackgroundColor(),
                data.getMousePointer(),
                toJson(backgrounds),
                toJson(images),
                toJson(texts)
        );

        Files.createDirectories(outputDir);
        Files.writeString(outputFile, html, StandardCharsets.UTF_8);
        return outputFile;
    }

    private Path findMostRecentGeneratedFile() {
        Path outputDir = getOutputDir();
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

    private List<BackgroundNodePayload> extractBackgroundNodes(MetadataDTO metadata, int parentWidth, int parentHeight) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return Collections.emptyList();
        }

        List<BackgroundNodePayload> backgrounds = new ArrayList<>();
        int index = 0;

        for (NodeDTO node : metadata.getSourceNodes()) {
            if (node == null || !BACKGROUND_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            NodeDataDTO data = node.getData();
            if (data == null) {
                continue;
            }

            int width = resolveParentSizedDimension(data.getWidth(), data.getAutoWidth(), parentWidth);
            int height = resolveParentSizedDimension(data.getHeight(), data.getAutoHeight(), parentHeight);
            String style = data.getStyle() == null ? "" : data.getStyle().trim();
            ImageNodePayload tileImage = extractFirstImageNode(data.getMetadata());

            backgrounds.add(new BackgroundNodePayload(
                    firstNonBlank(node.getNodeId(), node.getId(), "background-" + index),
                    style,
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    width,
                    height,
                    Boolean.TRUE.equals(data.getAutoWidth()),
                    Boolean.TRUE.equals(data.getAutoHeight()),
                    tileImage.getOpacity(),
                    TILE_STYLE.equalsIgnoreCase(style) ? tileImage : null
            ));

            index++;
        }

        return backgrounds;
    }

    private List<TextNodePayload> extractTextNodes(MetadataDTO metadata) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return Collections.emptyList();
        }

        List<TextNodePayload> texts = new ArrayList<>();
        for (NodeDTO node : metadata.getSourceNodes()) {
            if (node == null || !TEXT_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            NodeDataDTO data = node.getData();
            if (data == null || data.getText() == null || data.getText().isBlank()) {
                continue;
            }

            texts.add(new TextNodePayload(
                    data.getText(),
                    firstNonBlank(data.getFont(), "sans-serif"),
                    positiveIntOrDefault(data.getSize(), 16),
                    positiveIntOrDefault(data.getWidth(), 0),
                    positiveIntOrDefault(data.getHeight(), 0),
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    data.getOpacity() != null ? data.getOpacity() : 1.0,
                    Boolean.TRUE.equals(data.getBold()),
                    Boolean.TRUE.equals(data.getItalic()),
                    Boolean.TRUE.equals(data.getUnderline()),
                    Boolean.TRUE.equals(data.getStrikethrough()),
                    Boolean.TRUE.equals(data.getCaps())
            ));
        }

        return texts;
    }

    private ImageNodePayload extractFirstImageNode(MetadataDTO metadata) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return null;
        }

        for (NodeDTO node : metadata.getSourceNodes()) {
            if (node == null || !IMAGE_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            NodeDataDTO data = node.getData();
            if (data == null || data.getPath() == null || data.getPath().isBlank()) {
                continue;
            }

            return new ImageNodePayload(
                    data.getPath(),
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    data.getWidth(),
                    data.getHeight(),
                    Boolean.TRUE.equals(data.getAutoWidth()),
                    Boolean.TRUE.equals(data.getAutoHeight()),
                    data.getOpacity() != null ? data.getOpacity() : 1.0
            );
        }

        return null;
    }

    private String buildHtml(int canvasWidth,
                             int canvasHeight,
                             String backgroundColor,
                             String mousePointer,
                             String backgroundJson,
                             String imagesJson,
                             String textJson) {
        String safeBackgroundColor = backgroundColor == null ? "" : backgroundColor;
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
                      #stage { position: relative; width: __CANVAS_W__px; height: __CANVAS_H__px; overflow: hidden; }
                      #background-layer { position: absolute; inset: 0; z-index: 0; pointer-events: none; }
                      #image-layer { position: absolute; inset: 0; z-index: 1; pointer-events: none; }
                      #text-layer { position: absolute; inset: 0; z-index: 2; pointer-events: none; }
                      canvas { display: block; position: absolute; left: 0; top: 0; z-index: 3; }
                      #mouse-pointer {
                        position: absolute;
                        left: 0;
                        top: 0;
                        z-index: 4;
                        pointer-events: none;
                        display: none;
                        transform: translate(0px, 0px);
                      }
                    </style>
                    <script src="https://cdn.jsdelivr.net/npm/p5@1.9.0/lib/p5.min.js"></script>
                  </head>
                  <body>
                    <div id="stage">
                      <div id="background-layer"></div>
                      <div id="image-layer"></div>
                      <div id="text-layer"></div>
                      <img id="mouse-pointer" alt="mouse-pointer"/>
                    </div>
                    <script>
                      const BACKGROUND_NODES = __BACKGROUND_NODES__;
                      const IMAGE_NODES = __IMAGE_NODES__;
                      const TEXT_NODES = __TEXT_NODES__;
                      const PAGE_BACKGROUND_COLOR = __PAGE_BACKGROUND_COLOR__;
                      const MOUSE_POINTER_SRC = __MOUSE_POINTER_SRC__;
                      const CANVAS_W = __CANVAS_W__;
                      const CANVAS_H = __CANVAS_H__;

                      const TILE_STYLE = "tile";
                      const imageCache = new Map();
                      const stageElement = document.getElementById("stage");
                      const backgroundLayerElement = document.getElementById("background-layer");
                      const imageLayerElement = document.getElementById("image-layer");
                      const textLayerElement = document.getElementById("text-layer");
                      const mousePointerElement = document.getElementById("mouse-pointer");

                      function toProxyUrl(url) {
                        if (!url) return url;
                        const trimmed = url.trim();
                        if (!trimmed) return trimmed;
                        const lower = trimmed.toLowerCase();
                        if (lower.startsWith("http://") || lower.startsWith("https://")) {
                          return "https://corsproxy.io/?key=80b6bad2&url=" + encodeURIComponent(trimmed);
                        }
                        return trimmed;
                      }

                      function clamp01(value) {
                        const n = Number(value);
                        if (Number.isNaN(n)) return 1;
                        return Math.max(0, Math.min(1, n));
                      }

                      function isLikelyGif(src) {
                        if (!src) return false;
                        return /\\.gif(?:$|[?#])/i.test(src);
                      }

                      function getImageResource(src) {
                        if (!src) return null;
                        if (imageCache.has(src)) {
                          return imageCache.get(src);
                        }

                        const img = new Image();

                        const resource = {
                          src,
                          img,
                          loaded: false,
                          errored: false,
                          usedProxy: false,
                          onLoadCallbacks: []
                        };

                        img.onload = () => {
                          resource.loaded = true;
                          for (const callback of resource.onLoadCallbacks) {
                            try {
                              callback();
                            } catch (e) {
                              // noop
                            }
                          }
                        };

                        img.onerror = () => {
                          if (!resource.usedProxy && !isLikelyGif(src)) {
                            const lower = src.toLowerCase();
                            if (lower.startsWith("http://") || lower.startsWith("https://")) {
                              resource.usedProxy = true;
                              img.src = toProxyUrl(src);
                              return;
                            }
                          }
                          resource.errored = true;
                        };

                        // Prefer direct URL to preserve animated GIF behavior in canvas draws.
                        img.src = src.trim();
                        imageCache.set(src, resource);
                        return resource;
                      }

                      function imageNaturalSize(resource) {
                        const img = resource.img;
                        return {
                          width: img.naturalWidth || img.width || 0,
                          height: img.naturalHeight || img.height || 0
                        };
                      }

                      function resolveRenderSize(node, naturalWidth, naturalHeight) {
                        const hasWidth = Number(node.width) > 0;
                        const hasHeight = Number(node.height) > 0;
                        const width = node.autoWidth ? naturalWidth : (hasWidth ? Number(node.width) : naturalWidth);
                        const height = node.autoHeight ? naturalHeight : (hasHeight ? Number(node.height) : naturalHeight);
                        return {
                          width: Math.max(0, width),
                          height: Math.max(0, height)
                        };
                      }

                      function warmupResources() {
                        for (const node of IMAGE_NODES) {
                          getImageResource(node.src);
                        }

                        for (const node of BACKGROUND_NODES) {
                          if (node && node.tileImage && node.tileImage.src) {
                            getImageResource(node.tileImage.src);
                          }
                        }
                      }

                      function resolveNodeSurfaceSize(node) {
                        const width = node.autoWidth ? CANVAS_W : Math.max(0, Number(node.width) || 0);
                        const height = node.autoHeight ? CANVAS_H : Math.max(0, Number(node.height) || 0);
                        return { width, height };
                      }

                      function buildBackgroundNodes() {
                        backgroundLayerElement.innerHTML = "";

                        for (const node of BACKGROUND_NODES) {
                          if (!node) {
                            continue;
                          }

                          const surface = resolveNodeSurfaceSize(node);
                          if (surface.width <= 0 || surface.height <= 0) {
                            continue;
                          }

                          const wrapper = document.createElement("div");
                          wrapper.style.position = "absolute";
                          wrapper.style.left = `${node.x}px`;
                          wrapper.style.top = `${node.y}px`;
                          wrapper.style.width = `${surface.width}px`;
                          wrapper.style.height = `${surface.height}px`;
                          wrapper.style.overflow = "hidden";
                          wrapper.style.opacity = String(clamp01(node.opacity));

                          if ((node.style || "").toLowerCase() === TILE_STYLE && node.tileImage && node.tileImage.src) {
                            const tile = document.createElement("div");
                            tile.style.position = "absolute";
                            tile.style.inset = "0";
                            tile.style.backgroundRepeat = "repeat";

                            const tileSrc = node.tileImage.src.trim();
                            tile.style.backgroundImage = "url('" + encodeURI(tileSrc) + "')";

                            const resource = getImageResource(tileSrc);
                            const applyTileSize = () => {
                              if (!resource || !resource.loaded || resource.errored) {
                                return;
                              }
                              const natural = imageNaturalSize(resource);
                              const size = resolveRenderSize(node.tileImage, natural.width, natural.height);
                              if (size.width > 0 && size.height > 0) {
                                tile.style.backgroundSize = `${size.width}px ${size.height}px`;
                              }
                            };

                            if (resource && resource.loaded) {
                              applyTileSize();
                            } else if (resource) {
                              resource.onLoadCallbacks.push(applyTileSize);
                            }

                            wrapper.appendChild(tile);
                          }

                          backgroundLayerElement.appendChild(wrapper);
                        }
                      }

                      function setupMousePointer() {
                        if (!MOUSE_POINTER_SRC) {
                          mousePointerElement.style.display = "none";
                          cursor();
                          return;
                        }

                        const src = String(MOUSE_POINTER_SRC).trim();
                        if (!src) {
                          mousePointerElement.style.display = "none";
                          cursor();
                          return;
                        }

                        mousePointerElement.src = src;
                        mousePointerElement.style.display = "block";
                        noCursor();
                      }

                      function applyPageBackgroundColor() {
                        const color = typeof PAGE_BACKGROUND_COLOR === "string" ? PAGE_BACKGROUND_COLOR.trim() : "";
                        if (!color) {
                          return;
                        }
                        document.documentElement.style.backgroundColor = color;
                        document.body.style.backgroundColor = color;
                      }

                      function buildImageNodes() {
                        imageLayerElement.innerHTML = "";

                        for (const node of IMAGE_NODES) {
                          const resource = getImageResource(node.src);
                          if (!resource) {
                            continue;
                          }

                          const imageElement = document.createElement("img");
                          imageElement.style.position = "absolute";
                          imageElement.style.left = `${node.x}px`;
                          imageElement.style.top = `${node.y}px`;
                          imageElement.style.opacity = String(clamp01(node.opacity));
                          imageElement.style.pointerEvents = "none";
                          imageElement.decoding = "async";

                          const applyImageSize = () => {
                            if (!resource.loaded || resource.errored) {
                              return;
                            }
                            imageElement.src = resource.img.src;
                            const naturalSize = imageNaturalSize(resource);
                            const size = resolveRenderSize(node, naturalSize.width, naturalSize.height);
                            if (size.width > 0 && size.height > 0) {
                              imageElement.style.width = `${size.width}px`;
                              imageElement.style.height = `${size.height}px`;
                            }
                          };

                          if (resource.loaded) {
                            applyImageSize();
                          } else {
                            resource.onLoadCallbacks.push(applyImageSize);
                          }

                          imageElement.src = resource.img.src;
                          imageLayerElement.appendChild(imageElement);
                        }
                      }

                      function buildTextNodes() {
                        textLayerElement.innerHTML = "";

                        for (const node of TEXT_NODES) {
                          const width = Math.max(0, Number(node.width) || 0);
                          const height = Math.max(0, Number(node.height) || 0);
                          if (width <= 0 || height <= 0) {
                            continue;
                          }

                          const textElement = document.createElement("div");
                          textElement.style.position = "absolute";
                          textElement.style.left = `${Number(node.x) || 0}px`;
                          textElement.style.top = `${Number(node.y) || 0}px`;
                          textElement.style.width = `${width}px`;
                          textElement.style.height = `${height}px`;
                          textElement.style.opacity = String(clamp01(node.opacity));
                          textElement.style.overflow = "hidden";
                          textElement.style.whiteSpace = "pre-wrap";
                          textElement.style.wordBreak = "break-word";
                          textElement.style.fontFamily = (node.font || "sans-serif").trim() || "sans-serif";
                          textElement.style.fontSize = `${Math.max(1, Number(node.size) || 16)}px`;
                          textElement.style.fontWeight = node.bold ? "700" : "400";
                          textElement.style.fontStyle = node.italic ? "italic" : "normal";
                          textElement.style.textTransform = node.caps ? "uppercase" : "none";

                          const decorations = [];
                          if (node.underline) {
                            decorations.push("underline");
                          }
                          if (node.strikethrough) {
                            decorations.push("line-through");
                          }
                          textElement.style.textDecoration = decorations.length > 0 ? decorations.join(" ") : "none";
                          textElement.textContent = node.text || "";
                          textLayerElement.appendChild(textElement);
                        }
                      }

                      function drawMousePointer() {
                        if (mousePointerElement.style.display === "none") {
                          return;
                        }
                        mousePointerElement.style.transform = `translate(${mouseX}px, ${mouseY}px)`;
                      }

                      function setup() {
                        const canvas = createCanvas(CANVAS_W, CANVAS_H);
                        canvas.parent(stageElement);
                        applyPageBackgroundColor();
                        warmupResources();
                        buildBackgroundNodes();
                        buildImageNodes();
                        buildTextNodes();
                        setupMousePointer();

                        if (typeof window !== "undefined") {
                          window.addEventListener("mousemove", () => {
                            drawMousePointer();
                          });
                        }
                      }

                      function draw() {
                        clear();
                        drawMousePointer();
                      }
                    </script>
                  </body>
                </html>
                """;

        return template
                .replace("__BACKGROUND_NODES__", backgroundJson)
                .replace("__IMAGE_NODES__", imagesJson)
                .replace("__TEXT_NODES__", textJson)
                .replace("__PAGE_BACKGROUND_COLOR__", toJsonValue(safeBackgroundColor))
                .replace("__MOUSE_POINTER_SRC__", toJsonValue(safeMousePointer))
                .replace("__CANVAS_W__", String.valueOf(canvasWidth))
                .replace("__CANVAS_H__", String.valueOf(canvasHeight));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
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

    private int positiveIntOrDefault(Integer value, int defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private int resolveParentSizedDimension(Integer value, Boolean auto, int parentDimension) {
        if (Boolean.TRUE.equals(auto)) {
            return parentDimension;
        }
        return value != null ? value : parentDimension;
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return "";
        }

        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }

        return "";
    }

    private Path getOutputDir() {
        String envDir = System.getenv(OUTPUT_DIR_ENV);
        if (envDir != null && !envDir.isBlank()) {
            System.out.println("Using output directory from environment variable: " + envDir);
            return Paths.get(envDir.trim());
        }
        return Paths.get(OUTPUT_DIR_NAME);
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

    private static class BackgroundNodePayload {
        public final String cacheKey;
        public final String style;
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final boolean autoWidth;
        public final boolean autoHeight;
        public final double opacity;
        public final ImageNodePayload tileImage;

        private BackgroundNodePayload(String cacheKey,
                                      String style,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      boolean autoWidth,
                                      boolean autoHeight,
                                      double opacity,
                                      ImageNodePayload tileImage) {
            this.cacheKey = Objects.requireNonNullElse(cacheKey, "");
            this.style = Objects.requireNonNullElse(style, "");
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.autoWidth = autoWidth;
            this.autoHeight = autoHeight;
            this.opacity = opacity;
            this.tileImage = tileImage;
        }
    }
}

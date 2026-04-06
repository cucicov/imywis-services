package com.example.imywisservices.service;

import com.example.imywisservices.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
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
    private static final String EVENT_NODE_TYPE = "eventNode";
    private static final String EXTERNAL_LINK_NODE_TYPE = "externalLinkNode";
    private static final String DEFAULT_CLICK_TARGET_WINDOW = "_self";
    private static final String NEW_WINDOW_CLICK_TARGET = "_blank";
    private static final String TILE_STYLE = "tile";
    private static final Pattern USER_HANDLE_SANITIZER_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    private final AtomicReference<Path> lastGeneratedFile = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void generatePages(GraphDTO graph) throws Exception {
        if (graph == null || graph.getNodes() == null) {
            return;
        }

        if (graph.getNodes().isEmpty()) {
            throw new Exception("No nodes found in the graph");
        }

        String userHandle = sanitizeUserHandle(graph.getUserHandle());
        clearGeneratedPages(userHandle);
        lastGeneratedFile.set(null);

        Map<String, PageTargetConfig> pageTargetConfigs = collectPageTargetConfigs(graph.getNodes());

        for (NodeDTO node : graph.getNodes()) {
            if (node == null || !PAGE_NODE_TYPE.equals(node.getType())) {
                continue;
            }
            Path generated = generatePage(node, pageTargetConfigs, userHandle);
            if (generated != null) {
                lastGeneratedFile.set(generated);
            }
        }
    }

    private void clearGeneratedPages(String userHandle) throws Exception {
        Path outputDir = getUserOutputDir(userHandle);
        if (!Files.exists(outputDir)) {
            return;
        }

        if (!Files.isDirectory(outputDir)) {
            throw new Exception("Output path is not a directory: " + outputDir);
        }

        try (var stream = Files.list(outputDir)) {
            stream
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".html"))
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete generated page: " + path, e);
                        }
                    });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    public Path getLastGeneratedFile() {
        Path cached = lastGeneratedFile.get();
        if (cached != null && Files.exists(cached)) {
            return cached;
        }
        return findMostRecentGeneratedFile();
    }

    public Path getGeneratedPagesDir() {
        return getOutputDir();
    }

    public Path getGeneratedPagesDir(String userHandle) {
        return getUserOutputDir(userHandle);
    }

    private Path generatePage(NodeDTO pageNode, Map<String, PageTargetConfig> pageTargetConfigs, String userHandle) throws Exception {
        NodeDataDTO data = pageNode.getData();
        if (data == null || data.getName() == null || data.getName().isBlank()) {
            return null;
        }

        int canvasWidth = data.getWidth() != null ? data.getWidth() : DEFAULT_CANVAS_WIDTH;
        int canvasHeight = data.getHeight() != null ? data.getHeight() : DEFAULT_CANVAS_HEIGHT;

        String fileName = normalizeFileName(data.getName());
        Path outputDir = getUserOutputDir(userHandle);
        Path outputFile = outputDir.resolve(fileName);

        List<BackgroundNodePayload> backgrounds = extractBackgroundNodes(data.getMetadata(), canvasWidth, canvasHeight, pageTargetConfigs);
        List<ImageNodePayload> images = extractImageNodes(data.getMetadata(), pageTargetConfigs);
        List<TextNodePayload> texts = extractTextNodes(data.getMetadata(), pageTargetConfigs);

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

    private List<ImageNodePayload> extractImageNodes(MetadataDTO metadata, Map<String, PageTargetConfig> pageTargetConfigs) {
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
            ClickTargetPayload clickTarget = extractClickTarget(data.getMetadata(), pageTargetConfigs);
            images.add(new ImageNodePayload(
                    data.getPath(),
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    data.getWidth(),
                    data.getHeight(),
                    Boolean.TRUE.equals(data.getAutoWidth()),
                    Boolean.TRUE.equals(data.getAutoHeight()),
                    data.getOpacity() != null ? data.getOpacity() : 1.0,
                    clickTarget != null ? clickTarget.url() : null,
                    clickTarget != null ? clickTarget.windowTarget() : null,
                    clickTarget != null && clickTarget.popup(),
                    clickTarget != null ? clickTarget.popupWidth() : null,
                    clickTarget != null ? clickTarget.popupHeight() : null
            ));
        }
        return images;
    }

    private List<BackgroundNodePayload> extractBackgroundNodes(MetadataDTO metadata,
                                                               int parentWidth,
                                                               int parentHeight,
                                                               Map<String, PageTargetConfig> pageTargetConfigs) {
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
            TextNodePayload tileText = extractFirstTextNode(data.getMetadata(), pageTargetConfigs);
            ClickTargetPayload clickTarget = extractClickTarget(data.getMetadata(), pageTargetConfigs);
            double backgroundOpacity = tileImage != null
                    ? tileImage.getOpacity()
                    : (tileText != null
                    ? tileText.getOpacity()
                    : (data.getOpacity() != null ? data.getOpacity() : 1.0));

            backgrounds.add(new BackgroundNodePayload(
                    firstNonBlank(node.getNodeId(), node.getId(), "background-" + index),
                    style,
                    defaultInt(data.getPositionX()),
                    defaultInt(data.getPositionY()),
                    width,
                    height,
                    Boolean.TRUE.equals(data.getAutoWidth()),
                    Boolean.TRUE.equals(data.getAutoHeight()),
                    backgroundOpacity,
                    TILE_STYLE.equalsIgnoreCase(style) ? tileImage : null,
                    TILE_STYLE.equalsIgnoreCase(style) ? tileText : null,
                    clickTarget != null ? clickTarget.url() : null,
                    clickTarget != null ? clickTarget.windowTarget() : null,
                    clickTarget != null && clickTarget.popup(),
                    clickTarget != null ? clickTarget.popupWidth() : null,
                    clickTarget != null ? clickTarget.popupHeight() : null
            ));

            index++;
        }

        return backgrounds;
    }

    private List<TextNodePayload> extractTextNodes(MetadataDTO metadata, Map<String, PageTargetConfig> pageTargetConfigs) {
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

            ClickTargetPayload clickTarget = extractClickTarget(data.getMetadata(), pageTargetConfigs);
            texts.add(new TextNodePayload(
                    data.getText(),
                    data.getColor(),
                    data.getAlign(),
                    data.getBackgroundColor(),
                    !Boolean.FALSE.equals(data.getTransparentBackground()),
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
                    Boolean.TRUE.equals(data.getCaps()),
                    clickTarget != null ? clickTarget.url() : null,
                    clickTarget != null ? clickTarget.windowTarget() : null,
                    clickTarget != null && clickTarget.popup(),
                    clickTarget != null ? clickTarget.popupWidth() : null,
                    clickTarget != null ? clickTarget.popupHeight() : null
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
                    data.getOpacity() != null ? data.getOpacity() : 1.0,
                    null,
                    null,
                    false,
                    null,
                    null
            );
        }

        return null;
    }

    private TextNodePayload extractFirstTextNode(MetadataDTO metadata, Map<String, PageTargetConfig> pageTargetConfigs) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return null;
        }

        for (NodeDTO node : metadata.getSourceNodes()) {
            if (node == null || !TEXT_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            NodeDataDTO data = node.getData();
            if (data == null || data.getText() == null || data.getText().isBlank()) {
                continue;
            }

            ClickTargetPayload clickTarget = extractClickTarget(data.getMetadata(), pageTargetConfigs);
            return new TextNodePayload(
                    data.getText(),
                    data.getColor(),
                    data.getAlign(),
                    data.getBackgroundColor(),
                    !Boolean.FALSE.equals(data.getTransparentBackground()),
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
                    Boolean.TRUE.equals(data.getCaps()),
                    clickTarget != null ? clickTarget.url() : null,
                    clickTarget != null ? clickTarget.windowTarget() : null,
                    clickTarget != null && clickTarget.popup(),
                    clickTarget != null ? clickTarget.popupWidth() : null,
                    clickTarget != null ? clickTarget.popupHeight() : null
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
                      #background-layer { position: absolute; inset: 0; z-index: 0; }
                      #image-layer { position: absolute; inset: 0; z-index: 1; }
                      #text-layer { position: absolute; inset: 0; z-index: 2; }
                      canvas { display: block; position: absolute; left: 0; top: 0; z-index: 3; pointer-events: none; }
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
                      const clickableBindings = [];
                      let hasCustomMousePointer = false;

                      function resolveClickTarget(node) {
                        if (!node || typeof node.clickTarget !== "string") {
                          return null;
                        }
                        const url = node.clickTarget.trim();
                        if (!url) {
                          return null;
                        }
                        const requestedWindow = typeof node.clickTargetWindow === "string" ? node.clickTargetWindow.trim() : "";
                        const targetWindow = requestedWindow === "_blank" ? "_blank" : "_self";
                        const popup = Boolean(node.clickTargetPopup);
                        const popupWidth = Math.max(0, Number(node.clickTargetPopupWidth) || 0);
                        const popupHeight = Math.max(0, Number(node.clickTargetPopupHeight) || 0);
                        return { url, targetWindow, popup, popupWidth, popupHeight };
                      }

                      function bindClickRedirect(element, node) {
                        const clickBinding = resolveClickTarget(node);
                        if (!clickBinding) {
                          element.style.pointerEvents = "none";
                          element.style.cursor = "default";
                          element.removeAttribute("data-click-target");
                          element.removeAttribute("data-click-target-window");
                          element.removeAttribute("data-click-target-popup");
                          element.removeAttribute("data-click-target-popup-width");
                          element.removeAttribute("data-click-target-popup-height");
                          return;
                        }

                        const targetUrl = resolveNavigationUrl(clickBinding.url);
                        if (!targetUrl) {
                          element.style.pointerEvents = "none";
                          element.style.cursor = "default";
                          element.removeAttribute("data-click-target");
                          element.removeAttribute("data-click-target-window");
                          element.removeAttribute("data-click-target-popup");
                          element.removeAttribute("data-click-target-popup-width");
                          element.removeAttribute("data-click-target-popup-height");
                          return;
                        }

                        element.style.pointerEvents = "auto";
                        element.style.cursor = "pointer";
                        element.setAttribute("data-click-target", targetUrl);
                        element.setAttribute("data-click-target-window", clickBinding.targetWindow);
                        element.setAttribute("data-click-target-popup", clickBinding.popup ? "true" : "false");
                        if (clickBinding.popupWidth > 0) {
                          element.setAttribute("data-click-target-popup-width", String(clickBinding.popupWidth));
                        } else {
                          element.removeAttribute("data-click-target-popup-width");
                        }
                        if (clickBinding.popupHeight > 0) {
                          element.setAttribute("data-click-target-popup-height", String(clickBinding.popupHeight));
                        } else {
                          element.removeAttribute("data-click-target-popup-height");
                        }
                        clickableBindings.push({
                          element,
                          targetUrl,
                          targetWindow: clickBinding.targetWindow,
                          popup: clickBinding.popup,
                          popupWidth: clickBinding.popupWidth,
                          popupHeight: clickBinding.popupHeight
                        });
                      }

                      function openPopupWindow(targetUrl, popupWidth, popupHeight) {
                        const popupFeatures = ["popup=yes", "noopener", "noreferrer"];
                        if (popupWidth > 0) {
                          popupFeatures.push(`width=${Math.round(popupWidth)}`);
                        }
                        if (popupHeight > 0) {
                          popupFeatures.push(`height=${Math.round(popupHeight)}`);
                        }
                        return window.open(targetUrl, "_blank", popupFeatures.join(","));
                      }

                      function navigateToTarget(targetUrl, targetWindow, popup, popupWidth, popupHeight) {
                        if (popup) {
                          openPopupWindow(targetUrl, popupWidth, popupHeight);
                          return;
                        }
                        if (targetWindow === "_blank") {
                          window.open(targetUrl, "_blank", "noopener,noreferrer");
                        } else {
                          window.location.href = targetUrl;
                        }
                      }

                      function resolveNavigationUrl(clickTarget) {
                        if (!clickTarget) {
                          return "";
                        }

                        const target = String(clickTarget).trim();
                        if (!target) {
                          return "";
                        }

                        if (target.startsWith("/") || /^https?:\\/\\//i.test(target)) {
                          return target;
                        }
                        if (/^www\\./i.test(target)) {
                          return "https://" + target;
                        }
                        if (/^[a-z][a-z0-9+.-]*:/i.test(target)) {
                          return target;
                        }
                        const looksLikeBareDomain = /^[a-z0-9-]+(\\.[a-z0-9-]+)+(?:\\:\\d+)?(?:[/?#].*)?$/i.test(target);
                        const looksLikeInternalHtml = /\\.html?(?:$|[?#/])/i.test(target);
                        if (looksLikeBareDomain && !looksLikeInternalHtml) {
                          return "https://" + target;
                        }

                        const pathName = window.location.pathname || "/";
                        if (pathName.endsWith("/")) {
                          return pathName + target;
                        }

                        const lastSlash = pathName.lastIndexOf("/");
                        const lastSegment = lastSlash >= 0 ? pathName.slice(lastSlash + 1) : pathName;
                        const looksLikeFile = lastSegment.includes(".");
                        if (looksLikeFile && lastSlash >= 0) {
                          return pathName.slice(0, lastSlash + 1) + target;
                        }

        return pathName + "/" + target;
      }

                      function handleStageClick(event) {
                        const path = event.composedPath ? event.composedPath() : [];
                        for (const item of path) {
                          if (!item || !item.getAttribute) {
                            continue;
                          }
                          const directTarget = item.getAttribute("data-click-target");
                          if (directTarget) {
                            const directTargetWindow = item.getAttribute("data-click-target-window");
                            const directTargetPopup = item.getAttribute("data-click-target-popup") === "true";
                            const directTargetPopupWidth = Math.max(0, Number(item.getAttribute("data-click-target-popup-width")) || 0);
                            const directTargetPopupHeight = Math.max(0, Number(item.getAttribute("data-click-target-popup-height")) || 0);
                            navigateToTarget(directTarget, directTargetWindow, directTargetPopup, directTargetPopupWidth, directTargetPopupHeight);
                            return;
                          }
                        }

                        const clickX = event.clientX;
                        const clickY = event.clientY;
                        for (let i = clickableBindings.length - 1; i >= 0; i--) {
                          const binding = clickableBindings[i];
                          if (!binding || !binding.element || !binding.targetUrl) {
                            continue;
                          }
                          const rect = binding.element.getBoundingClientRect();
                          if (rect.width <= 0 || rect.height <= 0) {
                            continue;
                          }
                          const inside = clickX >= rect.left
                              && clickX <= rect.right
                              && clickY >= rect.top
                              && clickY <= rect.bottom;
                          if (inside) {
                            navigateToTarget(
                                binding.targetUrl,
                                binding.targetWindow,
                                Boolean(binding.popup),
                                Math.max(0, Number(binding.popupWidth) || 0),
                                Math.max(0, Number(binding.popupHeight) || 0)
                            );
                            return;
                          }
                        }
                      }

                      function updateStageCursor(event) {
                        let isOverClickable = false;

                        const path = event && event.composedPath ? event.composedPath() : [];
                        for (const item of path) {
                          if (!item || !item.getAttribute) {
                            continue;
                          }
                          const directTarget = item.getAttribute("data-click-target");
                          if (directTarget) {
                            isOverClickable = true;
                            break;
                          }
                        }

                        if (!isOverClickable) {
                          const moveX = event ? event.clientX : 0;
                          const moveY = event ? event.clientY : 0;
                          for (let i = clickableBindings.length - 1; i >= 0; i--) {
                            const binding = clickableBindings[i];
                            if (!binding || !binding.element || !binding.targetUrl) {
                              continue;
                            }
                            const rect = binding.element.getBoundingClientRect();
                            if (rect.width <= 0 || rect.height <= 0) {
                              continue;
                            }
                            const inside = moveX >= rect.left
                                && moveX <= rect.right
                                && moveY >= rect.top
                                && moveY <= rect.bottom;
                            if (inside) {
                              isOverClickable = true;
                              break;
                            }
                          }
                        }

                        if (isOverClickable) {
                          stageElement.style.cursor = "pointer";
                          if (hasCustomMousePointer) {
                            mousePointerElement.style.display = "none";
                          }
                          return;
                        }

                        if (hasCustomMousePointer) {
                          stageElement.style.cursor = "none";
                          mousePointerElement.style.display = "block";
                        } else {
                          stageElement.style.cursor = "default";
                          mousePointerElement.style.display = "none";
                        }
                      }

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
                          bindClickRedirect(wrapper, node);

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
                          } else if ((node.style || "").toLowerCase() === TILE_STYLE && node.tileText && node.tileText.text) {
                            const tileContent = document.createElement("div");
                            tileContent.style.position = "absolute";
                            tileContent.style.inset = "0";
                            tileContent.style.overflow = "hidden";

                            const tileSize = resolveTextTileSize(node.tileText);
                            for (let y = 0; y < surface.height; y += tileSize.height) {
                              for (let x = 0; x < surface.width; x += tileSize.width) {
                                const textTile = document.createElement("div");
                                applyTextNodeStyles(textTile, node.tileText, tileSize.width, tileSize.height);
                                textTile.style.left = `${x}px`;
                                textTile.style.top = `${y}px`;
                                tileContent.appendChild(textTile);
                              }
                            }

                            wrapper.appendChild(tileContent);
                          }

                          backgroundLayerElement.appendChild(wrapper);
                        }
                      }

                      function setupMousePointer() {
                        if (!MOUSE_POINTER_SRC) {
                          hasCustomMousePointer = false;
                          mousePointerElement.style.display = "none";
                          stageElement.style.cursor = "default";
                          return;
                        }

                        const src = String(MOUSE_POINTER_SRC).trim();
                        if (!src) {
                          hasCustomMousePointer = false;
                          mousePointerElement.style.display = "none";
                          stageElement.style.cursor = "default";
                          return;
                        }

                        hasCustomMousePointer = true;
                        mousePointerElement.src = src;
                        mousePointerElement.style.display = "block";
                        stageElement.style.cursor = "none";
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
                          imageElement.decoding = "async";
                          bindClickRedirect(imageElement, node);

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
                          applyTextNodeStyles(textElement, node, width, height);
                          textElement.style.left = `${Number(node.x) || 0}px`;
                          textElement.style.top = `${Number(node.y) || 0}px`;
                          bindClickRedirect(textElement, node);
                          textLayerElement.appendChild(textElement);
                        }
                      }

                      function resolveTextTileSize(textNode) {
                        const providedWidth = Math.max(0, Number(textNode.width) || 0);
                        const providedHeight = Math.max(0, Number(textNode.height) || 0);
                        const fontSize = Math.max(1, Number(textNode.size) || 16);
                        const textLength = Math.max(1, String(textNode.text || "").length);
                        const fallbackWidth = Math.ceil(fontSize * textLength * 0.6);
                        const fallbackHeight = Math.ceil(fontSize * 1.2);
                        return {
                          width: Math.max(1, providedWidth || fallbackWidth),
                          height: Math.max(1, providedHeight || fallbackHeight)
                        };
                      }

                      function applyTextNodeStyles(textElement, node, width, height) {
                        textElement.style.position = "absolute";
                        textElement.style.width = `${Math.max(0, Number(width) || 0)}px`;
                        textElement.style.height = `${Math.max(0, Number(height) || 0)}px`;
                        textElement.style.opacity = String(clamp01(node.opacity));
                        textElement.style.overflow = "hidden";
                        textElement.style.whiteSpace = "pre-wrap";
                        textElement.style.wordBreak = "break-word";
                        textElement.style.fontFamily = (node.font || "sans-serif").trim() || "sans-serif";
                        const textColor = typeof node.color === "string" ? node.color.trim() : "";
                        const textAlign = typeof node.align === "string" ? node.align.trim().toLowerCase() : "";
                        textElement.style.textAlign = (textAlign === "left" || textAlign === "right" || textAlign === "center") ? textAlign : "left";
                        textElement.style.color = textColor || "inherit";
                        const isTransparentBackground = node.transparentBackground !== false;
                        const textBackgroundColor = typeof node.backgroundColor === "string" ? node.backgroundColor.trim() : "";
                        textElement.style.backgroundColor = isTransparentBackground ? "transparent" : (textBackgroundColor || "transparent");
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
                        stageElement.addEventListener("click", handleStageClick, true);
                        stageElement.addEventListener("mousemove", updateStageCursor, true);
                        stageElement.addEventListener("mouseleave", () => {
                          if (hasCustomMousePointer) {
                            stageElement.style.cursor = "none";
                            mousePointerElement.style.display = "block";
                          } else {
                            stageElement.style.cursor = "default";
                            mousePointerElement.style.display = "none";
                          }
                        });
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

    private Path getUserOutputDir(String userHandle) {
        Path outputRoot = getOutputDir();
        String sanitizedUserHandle = sanitizeUserHandle(userHandle);
        if (sanitizedUserHandle == null) {
            return outputRoot;
        }
        return outputRoot.resolve(sanitizedUserHandle);
    }

    private String sanitizeUserHandle(String userHandle) {
        if (userHandle == null || userHandle.isBlank()) {
            return null;
        }

        String sanitized = USER_HANDLE_SANITIZER_PATTERN.matcher(userHandle.trim()).replaceAll("_");
        return sanitized.isBlank() ? null : sanitized;
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

    private Map<String, PageTargetConfig> collectPageTargetConfigs(List<NodeDTO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, PageTargetConfig> pageConfigs = new HashMap<>();
        for (NodeDTO node : nodes) {
            if (node == null || !PAGE_NODE_TYPE.equals(node.getType())) {
                continue;
            }

            NodeDataDTO data = node.getData();
            if (data == null || data.getName() == null || data.getName().isBlank()) {
                continue;
            }

            String targetPage = normalizeFileName(data.getName());
            int width = positiveIntOrDefault(data.getWidth(), DEFAULT_CANVAS_WIDTH);
            int height = positiveIntOrDefault(data.getHeight(), DEFAULT_CANVAS_HEIGHT);
            boolean popup = Boolean.TRUE.equals(data.getPopUp());
            pageConfigs.put(targetPage, new PageTargetConfig(popup, width, height));
        }

        return pageConfigs;
    }

    private ClickTargetPayload extractClickTarget(MetadataDTO metadata, Map<String, PageTargetConfig> pageTargetConfigs) {
        if (metadata == null || metadata.getSourceNodes() == null) {
            return null;
        }

        Deque<NodeDTO> queue = new ArrayDeque<>(metadata.getSourceNodes());
        Set<NodeDTO> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        while (!queue.isEmpty()) {
            NodeDTO sourceNode = queue.removeFirst();
            if (sourceNode == null || !visited.add(sourceNode)) {
                continue;
            }

            if (!EVENT_NODE_TYPE.equals(sourceNode.getType())) {
                MetadataDTO nestedMetadata = sourceNode.getData() != null ? sourceNode.getData().getMetadata() : null;
                if (nestedMetadata != null && nestedMetadata.getSourceNodes() != null) {
                    queue.addAll(nestedMetadata.getSourceNodes());
                }
                continue;
            }

            NodeDataDTO eventData = sourceNode.getData();
            if (eventData == null) {
                continue;
            }

            String eventType = eventData.getType() == null ? "" : eventData.getType().trim();
            if (!eventType.isEmpty() && !"click".equalsIgnoreCase(eventType)) {
                continue;
            }

            ClickTargetPayload eventTarget = extractEventTarget(eventData, pageTargetConfigs);
            if (eventTarget != null) {
                return eventTarget;
            }

            MetadataDTO nestedMetadata = eventData.getMetadata();
            if (nestedMetadata != null && nestedMetadata.getSourceNodes() != null) {
                queue.addAll(nestedMetadata.getSourceNodes());
            }
        }

        return null;
    }

    private ClickTargetPayload extractEventTarget(NodeDataDTO eventData, Map<String, PageTargetConfig> pageTargetConfigs) {
        if (eventData == null || eventData.getMetadata() == null || eventData.getMetadata().getSourceNodes() == null) {
            return null;
        }

        for (NodeDTO metadataNode : eventData.getMetadata().getSourceNodes()) {
            if (metadataNode == null) {
                continue;
            }

            if (PAGE_NODE_TYPE.equals(metadataNode.getType())) {
                NodeDataDTO data = metadataNode.getData();
                if (data == null || data.getName() == null || data.getName().isBlank()) {
                    continue;
                }

                String targetPage = normalizeFileName(data.getName());
                PageTargetConfig pageConfig = pageTargetConfigs != null ? pageTargetConfigs.get(targetPage) : null;
                if (pageConfig != null) {
                    return new ClickTargetPayload(
                            targetPage,
                            DEFAULT_CLICK_TARGET_WINDOW,
                            pageConfig.popup(),
                            pageConfig.width(),
                            pageConfig.height()
                    );
                }
                continue;
            }

            if (!EXTERNAL_LINK_NODE_TYPE.equals(metadataNode.getType())) {
                continue;
            }

            NodeDataDTO data = metadataNode.getData();
            if (data == null || data.getUrl() == null || data.getUrl().isBlank()) {
                continue;
            }

            return new ClickTargetPayload(
                    data.getUrl().trim(),
                    normalizeClickTargetWindow(data.getTarget()),
                    false,
                    null,
                    null
            );
        }

        return null;
    }

    private String normalizeClickTargetWindow(String requestedWindow) {
        String normalized = requestedWindow == null ? "" : requestedWindow.trim();
        if (NEW_WINDOW_CLICK_TARGET.equalsIgnoreCase(normalized)) {
            return NEW_WINDOW_CLICK_TARGET;
        }
        return DEFAULT_CLICK_TARGET_WINDOW;
    }

    private record ClickTargetPayload(String url,
                                      String windowTarget,
                                      boolean popup,
                                      Integer popupWidth,
                                      Integer popupHeight) {
    }

    private record PageTargetConfig(boolean popup, int width, int height) {
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
        public final TextNodePayload tileText;
        public final String clickTarget;
        public final String clickTargetWindow;
        public final boolean clickTargetPopup;
        public final Integer clickTargetPopupWidth;
        public final Integer clickTargetPopupHeight;

        private BackgroundNodePayload(String cacheKey,
                                      String style,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      boolean autoWidth,
                                      boolean autoHeight,
                                      double opacity,
                                      ImageNodePayload tileImage,
                                      TextNodePayload tileText,
                                      String clickTarget,
                                      String clickTargetWindow,
                                      boolean clickTargetPopup,
                                      Integer clickTargetPopupWidth,
                                      Integer clickTargetPopupHeight) {
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
            this.tileText = tileText;
            this.clickTarget = clickTarget;
            this.clickTargetWindow = clickTargetWindow;
            this.clickTargetPopup = clickTargetPopup;
            this.clickTargetPopupWidth = clickTargetPopupWidth;
            this.clickTargetPopupHeight = clickTargetPopupHeight;
        }
    }
}

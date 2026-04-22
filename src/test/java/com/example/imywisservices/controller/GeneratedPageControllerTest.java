package com.example.imywisservices.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServeUserIndexPageFromUserRootPath() throws Exception {
        Path userDir = Path.of("generated-pages", "alice");
        Files.createDirectories(userDir);
        Files.writeString(userDir.resolve("index.html"), "<!doctype html><html><body>alice-index</body></html>", StandardCharsets.UTF_8);

        mockMvc.perform(get("/alice"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice-index")));
    }

    @Test
    void shouldServeSpecificGeneratedPageFromUserRootPath() throws Exception {
        Path userDir = Path.of("generated-pages", "bob");
        Files.createDirectories(userDir);
        Files.writeString(userDir.resolve("about.html"), "<!doctype html><html><body>bob-about</body></html>", StandardCharsets.UTF_8);

        mockMvc.perform(get("/bob/about.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bob-about")));
    }

    @Test
    void shouldReturn404WhenUserPageDoesNotExist() throws Exception {
        mockMvc.perform(get("/missing-user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldServeUserImageAssetFromImgDirectory() throws Exception {
        Path userImgDir = Path.of("generated-pages", "bob", "img");
        Files.createDirectories(userImgDir);
        byte[] pngBytes = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0fQAAAAASUVORK5CYII=");
        Files.write(userImgDir.resolve("pixel.png"), pngBytes);

        mockMvc.perform(get("/img/bob/pixel.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(pngBytes));

        mockMvc.perform(get("/bob/img/pixel.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(pngBytes));
    }

    @Test
    void shouldServeRootImageAssetFromImgDirectory() throws Exception {
        Path rootImgDir = Path.of("generated-pages", "img");
        Files.createDirectories(rootImgDir);
        byte[] pngBytes = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0fQAAAAASUVORK5CYII=");
        Files.write(rootImgDir.resolve("pixel-root.png"), pngBytes);

        mockMvc.perform(get("/img/pixel-root.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(pngBytes));
    }
}

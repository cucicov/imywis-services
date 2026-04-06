package com.example.imywisservices.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GeneratedPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServeUserIndexPageFromTestPrefix() throws Exception {
        Path userDir = Path.of("generated-pages", "alice");
        Files.createDirectories(userDir);
        Files.writeString(userDir.resolve("index.html"), "<!doctype html><html><body>alice-index</body></html>", StandardCharsets.UTF_8);

        mockMvc.perform(get("/test/alice"))
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
        mockMvc.perform(get("/test/missing-user"))
                .andExpect(status().isNotFound());
    }
}

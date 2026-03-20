package com.example.imywisservices.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void shouldServeIndexPageByDefault() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<!doctype html>")));
    }

    @Test
    void shouldServeSpecificGeneratedPage() throws Exception {
        mockMvc.perform(get("/test/222.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<!doctype html>")));
    }
}

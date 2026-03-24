package com.example.imywisservices.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void testProcessNodes() throws Exception {
        String json = """
                {
                  "nodes": [
                    {
                      "id": "1",
                      "type": "pageNode",
                      "data": {
                        "label": "pageNode",
                        "name": "index.html",
                        "metadata": {
                          "sourceNodes": [
                            {
                              "nodeId": "2",
                              "nodeType": "imageNode",
                              "handleType": "red-output",
                              "data": {
                                "path": "asdasd",
                                "width": 100,
                                "height": "104",
                                "autoWidth": false,
                                "autoHeight": false,
                                "positionX": 0,
                                "positionY": 0,
                                "opacity": 1,
                                "metadata": {
                                  "sourceNodes": [
                                    {
                                      "nodeId": "3",
                                      "nodeType": "imageNode",
                                      "handleType": "orange-output",
                                      "data": {
                                        "path": "asd",
                                        "width": "4444",
                                        "height": 100,
                                        "autoWidth": false,
                                        "autoHeight": false,
                                        "positionX": 0,
                                        "positionY": 0,
                                        "opacity": 1
                                      }
                                    }
                                  ]
                                }
                              }
                            },
                            {
                              "nodeId": "4",
                              "nodeType": "imageNode",
                              "handleType": "red-output",
                              "data": {
                                "path": "asdsd",
                                "width": "101",
                                "height": 100,
                                "autoWidth": false,
                                "autoHeight": false,
                                "positionX": 0,
                                "positionY": 0,
                                "opacity": 1
                              }
                            }
                          ]
                        }
                      },
                      "position": {
                        "x": 250,
                        "y": 5
                      },
                      "measured": {
                        "width": 203,
                        "height": 680
                      },
                      "selected": true
                    },
                    {
                      "id": "2",
                      "type": "imageNode",
                      "data": {
                        "label": "imageNode",
                        "path": "asdasd",
                        "width": 100,
                        "height": "104",
                        "autoWidth": false,
                        "autoHeight": false,
                        "positionX": 0,
                        "positionY": 0,
                        "opacity": 1,
                        "metadata": {
                          "sourceNodes": [
                            {
                              "nodeId": "3",
                              "nodeType": "imageNode",
                              "handleType": "orange-output",
                              "data": {
                                "path": "asd",
                                "width": "4444",
                                "height": 100,
                                "autoWidth": false,
                                "autoHeight": false,
                                "positionX": 0,
                                "positionY": 0,
                                "opacity": 1
                              }
                            }
                          ]
                        }
                      },
                      "position": {
                        "x": 125.171182231496,
                        "y": -418.23288249579804
                      },
                      "measured": {
                        "width": 177,
                        "height": 348
                      },
                      "selected": false,
                      "dragging": false
                    },
                    {
                      "id": "3",
                      "type": "imageNode",
                      "data": {
                        "label": "imageNode",
                        "path": "asd",
                        "width": "4444",
                        "height": 100,
                        "autoWidth": false,
                        "autoHeight": false,
                        "positionX": 0,
                        "positionY": 0,
                        "opacity": 1
                      },
                      "position": {
                        "x": 306.8582167438241,
                        "y": -727.5666714242597
                      },
                      "measured": {
                        "width": 177,
                        "height": 158
                      },
                      "selected": false,
                      "dragging": false
                    },
                    {
                      "id": "4",
                      "type": "imageNode",
                      "data": {
                        "label": "imageNode",
                        "path": "asdsd",
                        "width": "101",
                        "height": 100,
                        "autoWidth": false,
                        "autoHeight": false,
                        "positionX": 0,
                        "positionY": 0,
                        "opacity": 1
                      },
                      "position": {
                        "x": 431.13510758691217,
                        "y": -389.1556830193049
                      },
                      "measured": {
                        "width": 177,
                        "height": 158
                      },
                      "selected": false,
                      "dragging": false
                    }
                  ],
                  "edges": [
                    {
                      "source": "3",
                      "sourceHandle": "orange-output",
                      "target": "2",
                      "targetHandle": "orange-input",
                      "id": "xy-edge__3orange-output-2orange-input"
                    },
                    {
                      "source": "2",
                      "sourceHandle": "red-output",
                      "target": "1",
                      "targetHandle": "red-input",
                      "id": "xy-edge__2red-output-1red-input"
                    },
                    {
                      "source": "4",
                      "sourceHandle": "red-output",
                      "target": "1",
                      "targetHandle": "red-input",
                      "id": "xy-edge__4red-output-1red-input"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nodes processed successfully"))
                .andExpect(jsonPath("$.nodes.length()").value(4))
                .andExpect(jsonPath("$.nodes[0].id").value("1"))
                .andExpect(jsonPath("$.nodes[0].data.name").value("index.html"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].nodeId").value("2"))
                .andExpect(jsonPath("$.nodes[1].id").value("2"))
                .andExpect(jsonPath("$.nodes[2].id").value("3"))
                .andExpect(jsonPath("$.nodes[3].id").value("4"));
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithBackgroundNode() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "label": "pageNode",
                      "name": "with-background.html",
                      "backgroundColor": "#cceeff",
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "7",
                            "type": "backgroundNode",
                            "handleType": "red-output",
                            "data": {
                              "style": "tile",
                              "width": 100,
                              "height": 100,
                              "autoWidth": true,
                              "autoHeight": true,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "8",
                                    "type": "imageNode",
                                    "handleType": "orange-output",
                                    "data": {
                                      "path": "https://example.com/bg.gif",
                                      "width": 16,
                                      "height": 16,
                                      "autoWidth": false,
                                      "autoHeight": false
                                    }
                                  }
                                ]
                              }
                            }
                          },
                          {
                            "nodeId": "9",
                            "type": "textNode",
                            "handleType": "red-output",
                            "data": {
                              "text": "Hello Text Node",
                              "font": "sans-serif",
                              "size": 16,
                              "width": 250,
                              "height": 300,
                              "positionX": 390,
                              "positionY": 300,
                              "opacity": 1,
                              "bold": true,
                              "italic": true,
                              "underline": true,
                              "strikethrough": true,
                              "caps": true
                            }
                          }
                        ]
                      },
                      "width": 800,
                      "height": 500
                    }
                  }
                ]
                """;

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.nodes.length()").value(1))
                .andExpect(jsonPath("$.nodes[0].data.backgroundColor").value("#cceeff"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].type").value("backgroundNode"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].data.style").value("tile"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].data.metadata.sourceNodes[0].type").value("imageNode"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].type").value("textNode"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.text").value("Hello Text Node"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.bold").value(true))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.italic").value(true))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.underline").value(true))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.strikethrough").value(true))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[1].data.caps").value(true));

        Path generatedFile = Path.of("generated-pages", "with-background.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("document.documentElement.style.backgroundColor = color"),
                "Generated HTML should apply page background color to the document root."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("const PAGE_BACKGROUND_COLOR = \"#cceeff\";"),
                "Generated HTML should include serialized page background color."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("const TEXT_NODES = [{\"text\":\"Hello Text Node\""),
                "Generated HTML should include serialized text nodes."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("textElement.style.textDecoration = decorations.length > 0 ? decorations.join(\" \") : \"none\";"),
                "Generated HTML should apply text decorations for text nodes."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithEventNodeRedirectAndMissingTargetSafety() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "name": "event-page",
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "3",
                            "type": "imageNode",
                            "data": {
                              "path": "https://example.com/clickable.jpg",
                              "positionX": 10,
                              "positionY": 20,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "4",
                                    "type": "eventNode",
                                    "data": {
                                      "type": "click",
                                      "metadata": {
                                        "sourceNodes": [
                                          {
                                            "nodeId": "2",
                                            "type": "pageNode",
                                            "data": {
                                              "name": "target-page"
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  }
                                ]
                              }
                            }
                          },
                          {
                            "nodeId": "5",
                            "type": "imageNode",
                            "data": {
                              "path": "https://example.com/non-clickable.jpg",
                              "positionX": 30,
                              "positionY": 40,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "6",
                                    "type": "eventNode",
                                    "data": {
                                      "type": "click"
                                    }
                                  }
                                ]
                              }
                            }
                          }
                        ]
                      }
                    }
                  },
                  {
                    "id": "2",
                    "type": "pageNode",
                    "data": {
                      "name": "target-page"
                    }
                  }
                ]
                """;

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.nodes.length()").value(2));

        Path generatedFile = Path.of("generated-pages", "event-page.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTarget\":\"target-page.html\""),
                "Generated HTML should include click target for valid event node page metadata."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTarget\":null"),
                "Generated HTML should keep click target null when event node has no valid page target."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("function handleStageClick(event) {"),
                "Generated HTML should include stage click handler for reliable click redirects."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("window.location.href = binding.targetUrl;"),
                "Generated HTML should redirect using click-hit detection when needed."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("stageElement.addEventListener(\"click\", handleStageClick, true);"),
                "Generated HTML should bind stage capture click listener."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("function updateStageCursor(event) {"),
                "Generated HTML should include cursor hover detection for clickable elements."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("stageElement.style.cursor = \"pointer\";"),
                "Generated HTML should set pointer cursor over clickable areas."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("canvas { display: block; position: absolute; left: 0; top: 0; z-index: 3; pointer-events: none; }"),
                "Generated HTML canvas should not intercept pointer events."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("function resolveNavigationUrl(clickTarget) {"),
                "Generated HTML should include relative navigation resolution for /test and /test/{page} routes."
        );
    }
}

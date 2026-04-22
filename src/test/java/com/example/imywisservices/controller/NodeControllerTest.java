package com.example.imywisservices.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

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
    public void testProcessNodesWithPagesDataAndUserId() throws Exception {
        String json = """
                {
                  "userid": "7f38e07d-54a5-441f-8699-fad202e10de0",
                  "userHandle": "cucicov",
                  "pagesData": [
                    {
                      "id": "1",
                      "type": "pageNode",
                      "position": {
                        "x": 301.6966897670149,
                        "y": -235.7127117276633
                      },
                      "data": {
                        "label": "pageNode",
                        "name": "index.html",
                        "backgroundColor": "#add5d5",
                        "metadata": {
                          "sourceNodes": [
                            {
                              "nodeId": "2",
                              "type": "textNode",
                              "handleType": "red-output",
                              "data": {
                                "text": "asdasdasda",
                                "color": "#000000",
                                "backgroundColor": "#ffffff",
                                "transparentBackground": true,
                                "align": "left",
                                "font": "sans-serif",
                                "size": 16,
                                "width": 250,
                                "height": 120,
                                "positionX": 0,
                                "positionY": 0,
                                "opacity": 1,
                                "bold": false,
                                "italic": false,
                                "underline": false,
                                "strikethrough": false,
                                "caps": false
                              }
                            }
                          ]
                        }
                      }
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
                .andExpect(jsonPath("$.userId").value("7f38e07d-54a5-441f-8699-fad202e10de0"))
                .andExpect(jsonPath("$.userHandle").value("cucicov"))
                .andExpect(jsonPath("$.nodes.length()").value(1))
                .andExpect(jsonPath("$.nodes[0].id").value("1"));

        Path generatedFile = Path.of("generated-pages", "cucicov", "index.html");
        org.junit.jupiter.api.Assertions.assertTrue(
                Files.exists(generatedFile),
                "POST /api/nodes with userHandle should write generated pages under generated-pages/{userHandle}."
        );
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
    public void testProcessNodesWithBackgroundNodeUsingTextTile() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "name": "background-text-tile",
                      "width": 320,
                      "height": 220,
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "7",
                            "type": "backgroundNode",
                            "data": {
                              "style": "tile",
                              "width": 140,
                              "height": 80,
                              "positionX": 20,
                              "positionY": 30,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "8",
                                    "type": "textNode",
                                    "data": {
                                      "text": "BG TXT",
                                      "font": "sans-serif",
                                      "size": 18,
                                      "width": 70,
                                      "height": 26,
                                      "opacity": 0.7,
                                      "bold": true
                                    }
                                  }
                                ]
                              }
                            }
                          }
                        ]
                      }
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
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].type").value("backgroundNode"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].data.metadata.sourceNodes[0].type").value("textNode"));

        Path generatedFile = Path.of("generated-pages", "background-text-tile.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"tileText\":{\"text\":\"BG TXT\""),
                "Generated HTML should serialize text tile payload for background nodes."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("} else if (nodeStyle === TILE_STYLE && node.tileText && node.tileText.text) {"),
                "Generated HTML should render tiled text backgrounds when background node source is a text node."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithBackgroundNodeUsingFullscreenImage() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "name": "background-fullscreen-image",
                      "width": 640,
                      "height": 360,
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "7",
                            "type": "backgroundNode",
                            "data": {
                              "style": "fullscreen",
                              "autoWidth": true,
                              "autoHeight": true,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "8",
                                    "type": "imageNode",
                                    "data": {
                                      "path": "https://example.com/fullscreen-bg.jpg",
                                      "width": 100,
                                      "height": 60
                                    }
                                  }
                                ]
                              }
                            }
                          }
                        ]
                      }
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
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].data.style").value("fullscreen"))
                .andExpect(jsonPath("$.nodes[0].data.metadata.sourceNodes[0].data.metadata.sourceNodes[0].type").value("imageNode"));

        Path generatedFile = Path.of("generated-pages", "background-fullscreen-image.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("const FULLSCREEN_STYLE = \"fullscreen\";"),
                "Generated HTML should declare fullscreen background style constant."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("} else if (nodeStyle === FULLSCREEN_STYLE && node.tileImage && node.tileImage.src) {"),
                "Generated HTML should render fullscreen backgrounds from image nodes."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("full.style.objectFit = \"cover\";"),
                "Generated HTML should use cover mode for fullscreen background images."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithBackgroundNodeUsingWrappedStyleValue() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "name": "background-wrapped-style",
                      "width": 320,
                      "height": 220,
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "7",
                            "type": "backgroundNode",
                            "data": {
                              "style": "style=\\"fullscreen\\"",
                              "autoWidth": true,
                              "autoHeight": true,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "8",
                                    "type": "imageNode",
                                    "data": {
                                      "path": "https://example.com/wrapped-style-bg.jpg",
                                      "width": 100,
                                      "height": 60
                                    }
                                  }
                                ]
                              }
                            }
                          }
                        ]
                      }
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
                .andExpect(jsonPath("$.nodes.length()").value(1));

        Path generatedFile = Path.of("generated-pages", "background-wrapped-style.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"style\":\"fullscreen\""),
                "Generated HTML should normalize wrapped style values to fullscreen."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesRemovesExistingGeneratedPagesBeforeWritingNewOnes() throws Exception {
        Path outputDir = Path.of("generated-pages");
        Files.createDirectories(outputDir);
        Path staleFile = outputDir.resolve("stale-page.html");
        Files.writeString(staleFile, "<html>stale</html>", StandardCharsets.UTF_8);

        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "data": {
                      "name": "fresh-page",
                      "width": 320,
                      "height": 220
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
                .andExpect(jsonPath("$.nodes.length()").value(1));

        Path freshFile = outputDir.resolve("fresh-page.html");
        org.junit.jupiter.api.Assertions.assertFalse(
                Files.exists(staleFile),
                "POST /api/nodes should remove previously generated HTML pages before writing new ones."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                Files.exists(freshFile),
                "POST /api/nodes should write the newly generated page."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithUserHandleRemovesExistingGeneratedAssetsRecursively() throws Exception {
        Path userDir = Path.of("generated-pages", "cleanup-user");
        Path staleHtml = userDir.resolve("stale.html");
        Path staleImg = userDir.resolve("img").resolve("stale.png");
        Files.createDirectories(staleImg.getParent());
        Files.writeString(staleHtml, "<html>stale</html>", StandardCharsets.UTF_8);
        Files.write(staleImg, new byte[]{1, 2, 3});

        String json = """
                {
                  "userHandle": "cleanup-user",
                  "nodes": [
                    {
                      "id": "1",
                      "type": "pageNode",
                      "data": {
                        "name": "fresh-user-page",
                        "width": 320,
                        "height": 220
                      }
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
                .andExpect(jsonPath("$.nodes.length()").value(1));

        Path freshFile = userDir.resolve("fresh-user-page.html");
        org.junit.jupiter.api.Assertions.assertFalse(
                Files.exists(staleHtml),
                "POST /api/nodes for a user handle should remove stale HTML files in that user's output directory."
        );
        org.junit.jupiter.api.Assertions.assertFalse(
                Files.exists(staleImg),
                "POST /api/nodes for a user handle should recursively remove stale assets like img files."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                Files.exists(freshFile),
                "POST /api/nodes for a user handle should write the newly generated page after cleanup."
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
                          },
                          {
                            "nodeId": "7",
                            "type": "imageNode",
                            "data": {
                              "path": "https://example.com/external-click.jpg",
                              "positionX": 50,
                              "positionY": 60,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "8",
                                    "type": "eventNode",
                                    "data": {
                                      "type": "click",
                                      "metadata": {
                                        "sourceNodes": [
                                          {
                                            "nodeId": "9",
                                            "type": "externalLinkNode",
                                            "data": {
                                              "url": "https://example.org/landing",
                                              "target": "_blank"
                                            }
                                          }
                                        ]
                                      }
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
                      "name": "target-page",
                      "width": 777,
                      "height": 555,
                      "popUp": true
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
                generatedHtml.contains("\"clickTarget\":\"https://example.org/landing\""),
                "Generated HTML should include click target for click event metadata external link nodes."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTargetWindow\":\"_blank\""),
                "Generated HTML should include click target window for click event metadata external link nodes."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTargetPopup\":true"),
                "Generated HTML should include click target popup flag for page nodes configured as popup."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTargetPopupWidth\":777"),
                "Generated HTML should include popup width from page node dimensions."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTargetPopupHeight\":555"),
                "Generated HTML should include popup height from page node dimensions."
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
                generatedHtml.contains("navigateToTarget("),
                "Generated HTML should route click redirects through navigation helper."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("window.open(targetUrl, \"_blank\", \"noopener,noreferrer\");"),
                "Generated HTML should open _blank click targets in a new tab."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("function openPopupWindow(targetUrl, popupWidth, popupHeight) {"),
                "Generated HTML should include popup window helper."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("popupFeatures.push(`width=${roundedPopupWidth}`);"),
                "Generated HTML should use popup width for popup page targets."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("popupFeatures.push(`height=${roundedPopupHeight}`);"),
                "Generated HTML should use popup height for popup page targets."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("popupFeatures.push(`left=${randomLeft}`);"),
                "Generated HTML should include randomized popup left position within viewport."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("popupFeatures.push(`top=${randomTop}`);"),
                "Generated HTML should include randomized popup top position within viewport."
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

    @Test
    @WithMockUser
    public void testProcessNodesWithTextNodeClickRedirect() throws Exception {
        String json = """
                [
                  {
                    "id": "1",
                    "type": "pageNode",
                    "position": {
                      "x": 481.78110199155026,
                      "y": 1305.8163908004324
                    },
                    "data": {
                      "label": "pageNode",
                      "name": "index.html",
                      "backgroundColor": "#ffbe6f",
                      "width": "1234",
                      "height": "1444",
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "4",
                            "type": "textNode",
                            "handleType": "red-output",
                            "data": {
                              "text": "Some text",
                              "font": "sans-serif",
                              "size": "33",
                              "width": 250,
                              "height": 120,
                              "positionX": "333",
                              "positionY": "333",
                              "opacity": 1,
                              "bold": false,
                              "italic": false,
                              "underline": false,
                              "strikethrough": false,
                              "caps": false,
                              "metadata": {
                                "sourceNodes": [
                                  {
                                    "nodeId": "5",
                                    "type": "eventNode",
                                    "handleType": "turquoise-output",
                                    "data": {
                                      "type": "click",
                                      "metadata": {
                                        "sourceNodes": [
                                          {
                                            "nodeId": "2",
                                            "type": "pageNode",
                                            "handleType": "red-input",
                                            "data": {
                                              "label": "pageNode",
                                              "name": "aaa",
                                              "width": "1002",
                                              "height": "1002",
                                              "mousePointer": "",
                                              "backgroundColor": "#ffffff"
                                            }
                                          }
                                        ]
                                      }
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
                    "position": {
                      "x": 254.30841747974222,
                      "y": 54.89792191938683
                    },
                    "data": {
                      "label": "pageNode",
                      "name": "aaa",
                      "width": "1002",
                      "height": "1002",
                      "mousePointer": "",
                      "backgroundColor": "#ffffff",
                      "metadata": {
                        "sourceNodes": [
                          {
                            "nodeId": "3",
                            "type": "imageNode",
                            "handleType": "red-output",
                            "data": {
                              "path": "https://www.ephotozine.com/resize/articles/22672/Lollycat.jpg?RTUdGk5cXyJFCgsJVANtdxU+cVRdHxFYFw1Gewk0T1JYFEtzen5YdgthHHsvEVxR",
                              "width": 100,
                              "height": 100,
                              "autoWidth": true,
                              "autoHeight": true,
                              "positionX": "222",
                              "positionY": "321",
                              "opacity": 1
                            }
                          }
                        ]
                      }
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

        Path generatedFile = Path.of("generated-pages", "index.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("const TEXT_NODES = [{\"text\":\"Some text\""),
                "Generated HTML should include serialized text node payload."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"clickTarget\":\"aaa.html\""),
                "Generated HTML should include click target for text node event redirect metadata."
        );
    }

    @Test
    @WithMockUser
    public void testProcessNodesWithLocalImageDataUrlUsesSavedImagePath() throws Exception {
        String localPngDataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0fQAAAAASUVORK5CYII=";
        byte[] pngBytes = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0fQAAAAASUVORK5CYII=");
        String expectedDigest = toSha256Hex(pngBytes);
        String expectedImagePath = "/img/local-image-user/" + expectedDigest + ".png";

        String json = """
                {
                  "userHandle": "local-image-user",
                  "nodes": [
                    {
                      "id": "1",
                      "type": "pageNode",
                      "data": {
                        "name": "local-image-page",
                        "metadata": {
                          "sourceNodes": [
                            {
                              "type": "imageNode",
                              "data": {
                                "path": "https://example.com/should-not-be-used.png",
                                "localImageDataUrl": "__LOCAL_IMAGE_DATA_URL__",
                                "positionX": 0,
                                "positionY": 0
                              }
                            }
                          ]
                        }
                      }
                    }
                  ]
                }
                """.replace("__LOCAL_IMAGE_DATA_URL__", localPngDataUrl);

        mockMvc.perform(post("/api/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.nodes.length()").value(1));

        Path generatedFile = Path.of("generated-pages", "local-image-user", "local-image-page.html");
        String generatedHtml = Files.readString(generatedFile, StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(
                generatedHtml.contains("\"src\":\"" + expectedImagePath + "\""),
                "Generated HTML should use saved local image path when localImageDataUrl is present."
        );
        org.junit.jupiter.api.Assertions.assertFalse(
                generatedHtml.contains("https://example.com/should-not-be-used.png"),
                "Generated HTML should not use remote path when localImageDataUrl is present."
        );

        Path savedImage = Path.of("generated-pages", "local-image-user", "img", expectedDigest + ".png");
        org.junit.jupiter.api.Assertions.assertTrue(
                Files.exists(savedImage),
                "Generator should save localImageDataUrl content under generated-pages/{userHandle}/img."
        );
    }

    private static String toSha256Hex(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input);
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}

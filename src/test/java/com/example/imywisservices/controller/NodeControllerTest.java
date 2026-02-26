package com.example.imywisservices.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].data.name").value("index.html"))
                .andExpect(jsonPath("$[0].data.metadata.sourceNodes[0].nodeId").value("2"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[2].id").value("3"))
                .andExpect(jsonPath("$[3].id").value("4"));
    }
}

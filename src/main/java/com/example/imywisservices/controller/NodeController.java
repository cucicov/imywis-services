package com.example.imywisservices.controller;

import com.example.imywisservices.dto.GraphDTO;
import com.example.imywisservices.dto.NodeDTO;
import com.example.imywisservices.service.GraphHtmlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final GraphHtmlService graphHtmlService;

    public NodeController(GraphHtmlService graphHtmlService) {
        this.graphHtmlService = graphHtmlService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processNodes(@RequestBody GraphDTO graph) {
        try {
            graphHtmlService.generatePages(graph);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nodes processed successfully");
            response.put("userId", graph.getUserId());
            response.put("userHandle", graph.getUserHandle());
            response.put("nodes", graph.getNodes());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process nodes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

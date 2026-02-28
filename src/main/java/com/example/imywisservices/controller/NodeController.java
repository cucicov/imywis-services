package com.example.imywisservices.controller;

import com.example.imywisservices.dto.GraphDTO;
import com.example.imywisservices.dto.NodeDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    @PostMapping
    public List<NodeDTO> processNodes(@RequestBody GraphDTO graph) {
        return graph.getNodes();
    }
}

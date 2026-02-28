package com.example.imywisservices.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDTO {
    private String id;
    private String nodeId;
    private String type;
    private NodeDataDTO data;
    private PositionDTO position;
}

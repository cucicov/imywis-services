package com.example.imywisservices.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//This DTO contains all fields cumulated from all the nodes
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDataDTO {
    private String label;
    private String path;
    private Integer width;
    private Integer height;
    private Boolean autoWidth;
    private Boolean autoHeight;
    private Integer positionX;
    private Integer positionY;
    private Double opacity;
    private Object mouse;
    private String mousePointer;
    private MetadataDTO metadata;
}

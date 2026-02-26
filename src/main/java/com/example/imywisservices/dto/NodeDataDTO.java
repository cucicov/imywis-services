package com.example.imywisservices.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDataDTO {
    private String label;
    private String name;
    private String path;
    private Object width;
    private Object height;
    private Boolean autoWidth;
    private Boolean autoHeight;
    private Double positionX;
    private Double positionY;
    private Double opacity;
    private MetadataDTO metadata;
}

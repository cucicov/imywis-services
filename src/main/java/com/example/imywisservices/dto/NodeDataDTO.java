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
    private String type;
    private String name;
    private String backgroundColor;
    private String path;
    private String url;
    private String target;
    private String text;
    private String color;
    private String align;
    private String font;
    private String style;
    private Integer size;
    private Integer width;
    private Integer height;
    private Boolean autoWidth;
    private Boolean autoHeight;
    private Boolean popUp;
    private Integer positionX;
    private Integer positionY;
    private Double opacity;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Boolean caps;
    private Boolean transparentBackground;
    private Object mouse;
    private String mousePointer;
    private MetadataDTO metadata;
}

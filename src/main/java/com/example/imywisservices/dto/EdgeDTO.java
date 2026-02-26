package com.example.imywisservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDTO {
    private String source;
    private String sourceHandle;
    private String target;
    private String targetHandle;
    private String id;
}

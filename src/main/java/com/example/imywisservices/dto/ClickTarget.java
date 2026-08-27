package com.example.imywisservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClickTarget {
    private String url;
    private String windowTarget;
    private boolean popup;
    private Integer popupWidth;
    private Integer popupHeight;
}

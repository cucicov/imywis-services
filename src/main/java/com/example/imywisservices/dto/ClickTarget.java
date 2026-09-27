package com.example.imywisservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class ClickTarget {
    private String url;
    private String windowTarget;
    private boolean popup;
    private Integer popupWidth;
    private Integer popupHeight;
    private List<SoundNodePayload> sounds;

    public ClickTarget(String url, String windowTarget, boolean popup, Integer popupWidth, Integer popupHeight) {
        this(url, windowTarget, popup, popupWidth, popupHeight, Collections.emptyList());
    }
}

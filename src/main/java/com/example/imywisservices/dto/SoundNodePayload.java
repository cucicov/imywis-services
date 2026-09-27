package com.example.imywisservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoundNodePayload {
    private String src;
    private boolean autoplay;
    private boolean loop;
}

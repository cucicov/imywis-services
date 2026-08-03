package com.example.imywisservices.dto;

import lombok.Data;

@Data
public class MaskNodePayload {
    public final int x;
    public final int y;
    public final Integer width;
    public final Integer height;
    public final String backgroundColor;
    public final String childType;
    public final ImageNodePayload image;
    public final TextNodePayload text;

    public MaskNodePayload(int x,
                           int y,
                           Integer width,
                           Integer height,
                           String backgroundColor,
                           String childType,
                           ImageNodePayload image,
                           TextNodePayload text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
        this.childType = childType;
        this.image = image;
        this.text = text;
    }
}

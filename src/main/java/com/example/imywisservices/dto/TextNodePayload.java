package com.example.imywisservices.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class TextNodePayload {
    public final String text;
    public final String font;
    public final int size;
    public final int width;
    public final int height;
    public final int x;
    public final int y;
    public final double opacity;
    public final boolean bold;
    public final boolean italic;
    public final boolean underline;
    public final boolean strikethrough;
    public final boolean caps;
    public final String clickTarget;

    public TextNodePayload(String text,
                           String font,
                           int size,
                           int width,
                           int height,
                           int x,
                           int y,
                           double opacity,
                           boolean bold,
                           boolean italic,
                           boolean underline,
                           boolean strikethrough,
                           boolean caps,
                           String clickTarget) {
        this.text = Objects.requireNonNullElse(text, "");
        this.font = Objects.requireNonNullElse(font, "sans-serif");
        this.size = size;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.opacity = opacity;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.caps = caps;
        this.clickTarget = clickTarget;
    }
}

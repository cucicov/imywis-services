package com.example.imywisservices.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class ImageNodePayload {
    public final String src;
    public final int x;
    public final int y;
    public final Integer width;
    public final Integer height;
    public final boolean autoWidth;
    public final boolean autoHeight;
    public final double opacity;
    public final String clickTarget;
    public final String clickTargetWindow;
    public final boolean clickTargetPopup;
    public final Integer clickTargetPopupWidth;
    public final Integer clickTargetPopupHeight;

    public ImageNodePayload(String src,
                             int x,
                             int y,
                             Integer width,
                             Integer height,
                             boolean autoWidth,
                             boolean autoHeight,
                             double opacity,
                             String clickTarget,
                             String clickTargetWindow,
                             boolean clickTargetPopup,
                             Integer clickTargetPopupWidth,
                             Integer clickTargetPopupHeight) {
        this.src = Objects.requireNonNull(src);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.autoWidth = autoWidth;
        this.autoHeight = autoHeight;
        this.opacity = opacity;
        this.clickTarget = clickTarget;
        this.clickTargetWindow = clickTargetWindow;
        this.clickTargetPopup = clickTargetPopup;
        this.clickTargetPopupWidth = clickTargetPopupWidth;
        this.clickTargetPopupHeight = clickTargetPopupHeight;
    }
}

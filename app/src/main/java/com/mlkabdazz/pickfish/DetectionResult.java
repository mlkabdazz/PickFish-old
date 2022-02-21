package com.mlkabdazz.pickfish;

import android.graphics.RectF;

public class DetectionResult {

    private RectF boundingBox;
    private String text;

    public DetectionResult(RectF boundingBox, String text) {
        this.boundingBox = boundingBox;
        this.text = text;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

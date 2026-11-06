package com.kawan.lotmanchors.client;

/** Coordinates are GUI-scaled pixels, not physical window pixels. */
public record AnchorPanelLayout(int left, int top, int width, int height) {
    public static AnchorPanelLayout fit(int screenWidth, int screenHeight, int maxWidth, int maxHeight) {
        int width = Math.max(1, Math.min(maxWidth, screenWidth - 16));
        int height = Math.max(1, Math.min(maxHeight, screenHeight - 16));
        return new AnchorPanelLayout((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }
    public int sourcePageSize() { return Math.max(1, (height - 180) / 32); }
    public int sourceBodyOffset(int categories) { return categories > 3 ? 22 : 0; }
    public int sourcePageSize(int categories) { return Math.max(1, (height - 180 - sourceBodyOffset(categories)) / 32); }
    public int scrollLimit(int contentHeight, int viewportHeight) { return Math.max(0, contentHeight - viewportHeight); }
}

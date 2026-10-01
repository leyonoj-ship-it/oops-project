package com.smartdine.model;

public enum AmbianceType {
    DEFAULT("White & Orange Minimalist", "#ff6b35", "#ffffff"),
    CANDLE_LIGHT("Candle Light Dinner", "#8b0000", "#1a0505"),
    ROMANTIC("Romantic Dining", "#d81b60", "#2c0b16"),
    FAMILY("Family Feast", "#e67e22", "#fffbf0"),
    FRIENDS("Friends Hangout", "#0284c7", "#f0f9ff"),
    PREMIUM("Premium Luxury", "#d4af37", "#121212"),
    CASUAL("Casual Dining", "#ff5722", "#f9fafb"),
    BIRTHDAY("Birthday Celebration", "#9c27b0", "#fcf4ff"),
    OUTDOOR("Outdoor Garden", "#2e7d32", "#f4fbf4"),
    QUIET_DINING("Quiet & Peaceful", "#37474f", "#eceff1");

    private final String displayName;
    private final String primaryColor;
    private final String backgroundColor;

    AmbianceType(String displayName, String primaryColor, String backgroundColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }
}
